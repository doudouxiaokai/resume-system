package com.jianlidb.resume.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Resume {
    private Long id;
    private Long userId;
    private String originalFilename;
    private String filePath;
    private Long fileSize;
    private String name;
    private String phone;
    private String email;
    private String gender;  // 新增：性别
    private Integer age;    // 新增：年龄
    private String skills;
    private String education;
    private String workExperience;
    private String rawText;
    private Integer status;
    private String parseResult;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}