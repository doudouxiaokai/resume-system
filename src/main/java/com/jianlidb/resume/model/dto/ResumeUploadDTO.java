package com.jianlidb.resume.model.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

@Data
public class ResumeUploadDTO {
    @NotNull(message = "文件不能为空")
    private MultipartFile file;
    private Long userId;
}