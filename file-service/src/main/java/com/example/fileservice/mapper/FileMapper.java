package com.example.fileservice.mapper;

/*
 *@author Yerlan
 *@create 2025-11-21 12:08
 */

import com.example.fileservice.dto.FileInfoDto;
import com.example.fileservice.model.FileEntity;

public class FileMapper {
    public static FileInfoDto toDto(FileEntity e) {
        FileInfoDto d = new FileInfoDto();
        d.setId(e.getId());
        d.setOriginalName(e.getOriginalName());
        d.setStoredName(e.getStoredName());
        d.setExtension(e.getExtension());
        d.setSize(e.getSize());
        d.setMimeType(e.getMimeType());
        d.setFolder(e.getFolder());
        d.setUploadDate(e.getUploadDate());
        return d;
    }
}

