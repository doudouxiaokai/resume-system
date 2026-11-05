package com.jianlidb.resume.model.dto;

import lombok.Data;

@Data
public class SearchRequestDTO {
    private String keyword;
    private Integer page = 1;
    private Integer size = 20;
}