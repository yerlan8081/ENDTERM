package com.example.yerlan_file.config;

/*
 *@author Yerlan
 *@create 2025-10-17 13:02
 */

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinIOConfig {

    @Value("http:localhost:9000")
    private String url;
    @Value("root")
    private String user;
    @Value("root123")
    private String password;

    @Bean
    public MinioClient minioClient(){
        return MinioClient
                .builder()
                .endpoint(url)
                .credentials(user,password)
                .build();
    }
}
