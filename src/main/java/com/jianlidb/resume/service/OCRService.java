package com.jianlidb.resume.service;

import org.springframework.web.multipart.MultipartFile;

public interface OCRService {

    /* ========== 兼容旧入口 ========== */
    String extractTextFromPdf(MultipartFile file) throws Exception;
    String extractTextFromImage(MultipartFile file) throws Exception;
    String extractText(MultipartFile file) throws Exception;

    /* ========== 新入口（字节数组，无需临时文件） ========== */
    String extractTextFromImage(byte[] imageBytes) throws Exception;
}