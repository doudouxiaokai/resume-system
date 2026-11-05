package com.jianlidb.resume.model.vo;

import lombok.Data;

@Data
public class SearchResultVO {
    private Long resumeId;
    private String name;
    private String skills;
    private String education;
    private Double relevance;
}