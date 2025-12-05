package com.example.yerlan_file.repository;

import com.example.yerlan_file.model.File;
import org.springframework.data.jpa.repository.JpaRepository;

/*
 *@author Yerlan
 *@create 2025-10-17 13:17
 */
public interface FileRepository extends JpaRepository<File, Long> {
    File findByName(String fileName);
}
