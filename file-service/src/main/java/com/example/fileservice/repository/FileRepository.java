package com.example.fileservice.repository;

/*
 *@author Yerlan
 *@create 2025-11-21 12:08
 */

import com.example.fileservice.model.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
    List<FileEntity> findByFolder(String folder);
    long countByFolder(String folder);
    void deleteByFolder(String folder);


}



