package com.example.fileservice.model;

/*
 *@author Yerlan
 *@create 2025-11-21 12:08
 */

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "files")
@Data
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalName;
    private String storedName;
    private String extension;
    private String mimeType;
    private Long size;
    private String folder;  // 新增字段，用来记录文件夹名称
    private LocalDateTime uploadDate;
}
