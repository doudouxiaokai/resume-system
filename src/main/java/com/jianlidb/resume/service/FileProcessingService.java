package com.jianlidb.resume.service;

import com.jianlidb.resume.model.domain.ResumeInfo;
import org.springframework.web.multipart.MultipartFile;

public interface FileProcessingService {

    /* ========== 兼容旧入口（MultipartFile） ========== */
    ResumeInfo processFile(MultipartFile file) throws Exception;
    String extractTextFromFile(MultipartFile file) throws Exception;

    /* ========== 新入口（主线程已读字节） ========== */
    ResumeInfo processFile(byte[] fileBytes, String filePath) throws Exception;
    String extractTextFromFile(byte[] fileBytes, String filePath) throws Exception;
}