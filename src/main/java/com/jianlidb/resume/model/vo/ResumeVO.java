package com.jianlidb.resume.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResumeVO {
    private Long id;
    private Long userId;
    private String originalFilename;
    private String name;
    private String phone;
    private String email;
    private String gender;  // 新增：性别
    private Integer age;    // 新增：年龄
    private String skills;
    private String education;
    private String workExperience;
    private Integer status;
    private LocalDateTime createdAt;
}