package com.EVO.storage_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadConfig {
    private long maxSize;
    private Set<String> allowedExtensions;
    private Set<String> allowedMimeTypes;
}