package com.jianlidb.resume.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class AIChatRequest {
    private String model;
    private List<Message> messages;
    private Double temperature;
    private Integer maxTokens;

    @Data
    public static class Message {
        private String role;
        private String content;
    }
}