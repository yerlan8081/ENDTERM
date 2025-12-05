package com.example.yerlan_file.mapper;

/*
 *@author Yerlan
 *@create 2025-10-17 12:55
 */
import com.example.yerlan_file.dto.FileDto;
import com.example.yerlan_file.model.File;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FileMapper {
    FileDto toDto(File file);
    File toEntity(FileDto dto);
    List<FileDto> toDtoList(List<File> fileList);

}
