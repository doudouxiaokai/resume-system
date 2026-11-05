package com.jianlidb.resume.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.ocr.baidu")
public class BaiduOCRConfig {
    private String appId;
    private String apiKey;
    private String secretKey;
    private String url;
    private String languageType = "CHN_ENG";
    private Boolean detectDirection = true;
}