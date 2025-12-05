package com.example.fileservice.controllers;

/*
 *@author Yerlan
 *@create 2025-11-21 12:06
 */

import com.example.fileservice.dto.FileInfoDto;
import com.example.fileservice.dto.FileUploadResponse;
import com.example.fileservice.mapper.FileMapper;
import com.example.fileservice.model.FileEntity;
import com.example.fileservice.services.FileService;
import io.minio.GetObjectResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> upload(@RequestParam("file") MultipartFile file) throws Exception {
        FileEntity saved = fileService.upload(file);
        return ResponseEntity.ok(new FileUploadResponse(saved.getId(), saved.getOriginalName(), saved.getStoredName(), saved.getSize()));
    }

    @PostMapping("/upload-multiple/{folder}")
    public ResponseEntity<List<FileUploadResponse>> uploadMultiple(
            @PathVariable String folder,
            @RequestParam("files") List<MultipartFile> files
    ) throws Exception {
        return ResponseEntity.ok(fileService.uploadMultiple(files, folder));
    }


    @GetMapping("/info/{id}")
    public ResponseEntity<FileInfoDto> info(@PathVariable Long id) {
        FileEntity e = fileService.info(id);
        return ResponseEntity.ok(FileMapper.toDto(e));
    }

    @GetMapping("/list/{folder}")
    public ResponseEntity<List<FileInfoDto>> listFiles(@PathVariable String folder) {

        List<FileEntity> entities = fileService.listFilesByFolder(folder);

        // 将实体转换成 DTO
        List<FileInfoDto> result = entities.stream()
                .map(FileMapper::toDto)
                .toList();

        return ResponseEntity.ok(result);
    }


    @GetMapping("/download/{id}")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long id) throws Exception {
        FileEntity meta = fileService.info(id);

        GetObjectResponse objectResponse = fileService.download(id);
        InputStreamResource resource = new InputStreamResource(objectResponse);

        // 使用 MIME 类型
        MediaType mediaType = MediaType.parseMediaType(meta.getMimeType());

        // 避免中文/空格文件名乱码
        String encodedFileName = URLEncoder.encode(meta.getOriginalName(), StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedFileName)
                .contentType(mediaType)              // 🔥 用真实 MIME，不再是 octet-stream
                .contentLength(meta.getSize())
                .body(resource);
    }

    @PostMapping("/download-zip")
    public ResponseEntity<InputStreamResource> downloadZip(@RequestBody List<Long> ids) throws Exception {

        ByteArrayOutputStream baos = fileService.createZip(ids);
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(baos.toByteArray()));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"files.zip\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(baos.size())
                .body(resource);
    }

    @GetMapping("/download-folder/{folder}")
    public ResponseEntity<InputStreamResource> downloadFolderZip(
            @PathVariable String folder) throws Exception {

        ByteArrayOutputStream baos = fileService.createZipByFolder(folder);
        byte[] zipBytes = baos.toByteArray();

        InputStreamResource resource =
                new InputStreamResource(new ByteArrayInputStream(zipBytes));

        // 正确处理文件名，包括 URL encode
        String zipName = folder + ".zip";
        String encoded = URLEncoder.encode(zipName, StandardCharsets.UTF_8);

        String contentDisposition = String.format(
                "attachment; filename=\"%s\"; filename*=UTF-8''%s",
                zipName,
                encoded
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .contentType(MediaType.parseMediaType("application/zip"))
                .contentLength(zipBytes.length)
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFile(@PathVariable Long id) throws Exception {
        fileService.deleteFileById(id);
        return ResponseEntity.ok("File deleted successfully.");
    }

    @DeleteMapping("/folder/{folder}")
    public ResponseEntity<String> deleteFolder(@PathVariable String folder) throws Exception {
        fileService.deleteFolder(folder);
        return ResponseEntity.ok("Folder deleted successfully.");
    }

}

