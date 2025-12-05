package com.example.fileservice.services;

/*
 *@author Yerlan
 *@create 2025-11-21 12:09
 */

import com.example.fileservice.dto.FileUploadResponse;
import com.example.fileservice.model.FileEntity;
import com.example.fileservice.repository.FileRepository;
import io.minio.*;
import io.minio.messages.Item;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class FileService {

    private final MinioClient minioClient;
    private final FileRepository fileRepository;

    @Value("${minio.bucket}")
    private String bucket;

    public FileEntity info(Long id) {
        return fileRepository.findById(id).orElseThrow(() -> new RuntimeException("File not found"));
    }

    public List<FileEntity> listFilesByFolder(String folder) {
        folder = folder.replaceAll("/$", ""); // 防止末尾多 "/"
        return fileRepository.findByFolder(folder);
    }


    public FileEntity upload(MultipartFile file) throws Exception {

        String original = Objects.requireNonNull(file.getOriginalFilename());
        String ext = "";

        int idx = original.lastIndexOf(".");
        if (idx != -1) ext = original.substring(idx);

        String storedName = UUID.randomUUID().toString() + ext;

        try (InputStream is = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(storedName)
                            .stream(is, file.getSize(), -1)
                            .contentType(file.getContentType())   // 🔥 保存 MIME 到 MinIO
                            .build()
            );
        }

        FileEntity entity = new FileEntity();
        entity.setOriginalName(original);
        entity.setStoredName(storedName);
        entity.setExtension(ext);
        entity.setSize(file.getSize());
        entity.setMimeType(file.getContentType());     // 🔥 保存 MIME 类型
        entity.setUploadDate(LocalDateTime.now());

        return fileRepository.save(entity);
    }

    public List<FileUploadResponse> uploadMultiple(List<MultipartFile> files, String folder) throws Exception {

        List<FileUploadResponse> responses = new ArrayList<>();

        // 确保 folder 末尾没有 "/"
        folder = folder.replaceAll("/$", "");

        for (MultipartFile file : files) {

            String original = Objects.requireNonNull(file.getOriginalFilename());
            String ext = "";
            int idx = original.lastIndexOf(".");
            if (idx != -1) ext = original.substring(idx);

            String uuidName = UUID.randomUUID() + ext;

            // 存储路径： folder/uuid.ext
            String storedPath = folder + "/" + uuidName;

            try (InputStream is = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucket)
                                .object(storedPath)
                                .stream(is, file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );
            }

            FileEntity e = new FileEntity();
            e.setOriginalName(original);
            e.setStoredName(storedPath);
            e.setExtension(ext);
            e.setSize(file.getSize());
            e.setMimeType(file.getContentType());
            e.setFolder(folder);
            e.setUploadDate(LocalDateTime.now());
            fileRepository.save(e);

            responses.add(new FileUploadResponse(
                    e.getId(),
                    e.getOriginalName(),
                    e.getStoredName(),
                    e.getSize()
            ));
        }

        return responses;
    }



    public GetObjectResponse download(Long id) throws Exception {
        FileEntity e = fileRepository.findById(id).orElseThrow(() -> new RuntimeException("File not found"));
        return minioClient.getObject(GetObjectArgs.builder().bucket(bucket).object(e.getStoredName()).build());
    }

    public ByteArrayOutputStream createZip(List<Long> ids) throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);

        for (Long id : ids) {

            FileEntity fileEntity = fileRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("File not found: " + id));

            // 从 MinIO 获取 InputStream
            GetObjectResponse object = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(fileEntity.getStoredName())
                            .build()
            );

            zos.putNextEntry(new ZipEntry(fileEntity.getOriginalName()));
            object.transferTo(zos);
            zos.closeEntry();
        }

        zos.close();
        return baos;
    }

    public ByteArrayOutputStream createZipByFolder(String folder) throws Exception {

        List<FileEntity> files = fileRepository.findByFolder(folder);
        if (files.isEmpty()) {
            throw new RuntimeException("Folder is empty or not found: " + folder);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);

        for (FileEntity file : files) {

            // 从 MinIO 获取文件内容
            GetObjectResponse object = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(file.getStoredName())   // folder/uuid.ext
                            .build()
            );

            // ZIP 内保存为 originalName，例如 photo.jpg
            zos.putNextEntry(new ZipEntry(file.getOriginalName()));
            object.transferTo(zos);
            zos.closeEntry();
        }

        zos.close();
        return baos;
    }

    public void deleteFileById(Long id) throws Exception {

        FileEntity file = fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found: " + id));

        String folder = file.getFolder();

        // 删除 MinIO 中的文件
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucket)
                        .object(file.getStoredName())
                        .build()
        );

        // 删除 DB 中的记录
        fileRepository.deleteById(id);

        // 如果这是最后一个文件 → 删除整个 folder
        long count = fileRepository.countByFolder(folder);

        if (count == 0) {
            deleteFolderFromMinio(folder);  // ← 就是这里
        }
    }

    // ⬇⬇⬇ 必须把这个方法放在下面 ⬇⬇⬇
    private void deleteFolderFromMinio(String folder) throws Exception {

        String prefix = folder.endsWith("/") ? folder : folder + "/";

        Iterable<Result<Item>> items = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucket)
                        .prefix(prefix)
                        .recursive(true)
                        .build()
        );

        for (Result<Item> item : items) {
            String objectName = item.get().objectName();

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );
        }
    }

    @Transactional
    public void deleteFolder(String folder) throws Exception {

        // 确保 folder 不以 / 结尾
        folder = folder.replaceAll("/$", "");

        List<FileEntity> files = fileRepository.findByFolder(folder);

        if (files.isEmpty()) {
            throw new RuntimeException("Folder not found or empty: " + folder);
        }

        // 1. 删除 MinIO 中所有文件
        deleteFolderFromMinio(folder);

        // 2. 删除数据库记录
        fileRepository.deleteByFolder(folder);
    }

}

