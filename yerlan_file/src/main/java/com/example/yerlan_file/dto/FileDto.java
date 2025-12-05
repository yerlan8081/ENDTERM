package com.example.yerlan_file.dto;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
 *@author Yerlan
 *@create 2025-10-17 12:48
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileDto {
    private Long id;
    private String orginalName;
    private String fileName;
    private String mimetype;
    private Long size;
    private LocalDateTime addedTime;

}
