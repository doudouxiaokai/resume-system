package com.jianlidb.resume.model.dto;

import lombok.Data;

@Data
public class AdvancedSearchRequestDTO {
    private String keyword;
    private String gender;
    private Integer minAge;
    private Integer maxAge;
    private String sortField = "createdAt";
    private String sortOrder = "desc";
    private Integer page = 1;
    private Integer size = 20;
}