package com.jianlidb.resume.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadConfig {
    private String path = "./uploads/";
    private List<String> allowedExtensions = Arrays.asList("pdf", "jpg", "jpeg", "png");
    private Long maxSize = 10 * 1024 * 1024L;
    private Boolean createDirectoryIfNotExists = true;
}