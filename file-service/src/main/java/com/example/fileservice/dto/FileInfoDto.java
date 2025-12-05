package com.example.fileservice.dto;

/*
 *@author Yerlan
 *@create 2025-11-21 12:07
 */

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileInfoDto {
    private Long id;
    private String originalName;
    private String storedName;
    private String extension;
    private Long size;
    private String mimeType;
    private String folder;
    private LocalDateTime uploadDate;
}

