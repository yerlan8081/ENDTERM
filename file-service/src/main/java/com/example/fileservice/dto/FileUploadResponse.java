package com.example.fileservice.dto;

/*
 *@author Yerlan
 *@create 2025-11-21 12:07
 */

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileUploadResponse {
    private Long id;
    private String originalName;
    private String storedName;
    private Long size;
}

