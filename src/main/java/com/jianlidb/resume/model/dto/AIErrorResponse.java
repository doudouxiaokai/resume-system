package com.jianlidb.resume.model.dto;

import lombok.Data;

@Data
public class AIErrorResponse {
    private Error error;

    @Data
    public static class Error {
        private String message;
        private String type;
    }
}