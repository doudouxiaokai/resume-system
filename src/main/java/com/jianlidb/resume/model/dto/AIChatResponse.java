package com.jianlidb.resume.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class AIChatResponse {
    private String id;
    private String object;
    private Long created;
    private String model;
    private List<Choice> choices;

    @Data
    public static class Choice {
        private Integer index;
        private Message message;

        @Data
        public static class Message {
            private String role;
            private String content;
        }
    }
}