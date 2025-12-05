package com.example.yerlan_file.model;

/*
 *@author Yerlan
 *@create 2025-10-17 12:28
 */

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "minio_file")
@AllArgsConstructor
@NoArgsConstructor
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String orginalName;
    private Long size;
    private String mimetype;

    @Column(unique = true)
    private String fileName;

    private LocalDateTime addedTime;

    @PrePersist
    private void setTime(){ addedTime = LocalDateTime.now();}
}
