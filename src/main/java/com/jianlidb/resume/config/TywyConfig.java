package com.jianlidb.resume.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.llm.tywy")
public class TywyConfig {
    private String apiKey;
    private String url;
    private String model = "qwen-turbo";
    private Double temperature = 0.3;
    private Integer maxTokens = 1000;
    private Integer timeout = 30;
    // 添加其他可能的配置参数
    private Double topP = 0.85;
    private Integer seed = 1234;
}