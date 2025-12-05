package com.example.endterm;

/*
 *@author Yerlan
 *@create 2025-12-05 18:51
 */
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    private void uploadToMinio(MultipartFile file, String expectedType) throws Exception {
        if (!file.getContentType().equals(expectedType)) {
            throw new IllegalArgumentException("文件类型错误！要求类型: " + expectedType);
        }

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(file.getOriginalFilename())
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );
    }

    @PostMapping("/upload/txt")
    public ResponseEntity<String> uploadTxt(@RequestParam("file") MultipartFile file) {
        try {
            uploadToMinio(file, "text/plain");
            return ResponseEntity.ok("TXT 文件上传成功: " + file.getOriginalFilename());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/upload/json")
    public ResponseEntity<String> uploadJson(@RequestParam("file") MultipartFile file) {
        try {
            uploadToMinio(file, "application/json");
            return ResponseEntity.ok("JSON 文件上传成功: " + file.getOriginalFilename());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/upload/png")
    public ResponseEntity<String> uploadPng(@RequestParam("file") MultipartFile file) {
        try {
            uploadToMinio(file, "image/png");
            return ResponseEntity.ok("PNG 文件上传成功: " + file.getOriginalFilename());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

