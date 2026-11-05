package com.jianlidb.resume.service.impl;

import com.jianlidb.resume.model.domain.ResumeInfo;
import com.jianlidb.resume.service.AIParseService;
import com.jianlidb.resume.service.FileProcessingService;
import com.jianlidb.resume.service.OCRService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Slf4j
@Service
public class FileProcessingServiceImpl implements FileProcessingService {

    @Autowired
    private OCRService ocrService;

    @Autowired
    private AIParseService aiParseService;

    /* ===================== 旧入口（保持兼容） ===================== */
    @Override
    public ResumeInfo processFile(MultipartFile file) throws Exception {
        log.info("开始处理文件: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());
        String text = extractTextFromFile(file);
        if (text == null || text.trim().isEmpty()) {
            throw new RuntimeException("无法从文件中提取文本内容");
        }
        log.info("从文件中提取文本长度: {} 字符", text.length());
        return aiParseService.parseResumeText(text);
    }

    @Override
    public String extractTextFromFile(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new RuntimeException("文件名不能为空");
        }
        String lowerName = filename.toLowerCase();
        if (lowerName.endsWith(".pdf")) {
            // 把 MultipartFile 先落盘再读
            File temp = File.createTempFile("resume", ".pdf");
            file.transferTo(temp);
            String text = extractTextWithPDFBox(temp);
            temp.deleteOnExit();
            return text;
        } else {
            // 图片直接走内存
            return ocrService.extractTextFromImage(file.getBytes());
        }
    }

    /* ===================== 新入口（主线程已读字节） ===================== */
    @Override
    public ResumeInfo processFile(byte[] fileBytes, String filePath) throws Exception {
        log.info("开始处理文件: {}, 大小: {} bytes", filePath, fileBytes.length);
        String text = extractTextFromFile(fileBytes, filePath);
        if (text == null || text.trim().isEmpty()) {
            throw new RuntimeException("无法从文件中提取文本内容");
        }
        log.info("从文件中提取文本长度: {} 字符", text.length());
        return aiParseService.parseResumeText(text);
    }

    @Override
    public String extractTextFromFile(byte[] fileBytes, String filePath) throws Exception {
        String lowerName = filePath.toLowerCase();
        if (lowerName.endsWith(".pdf")) {
            // PDF 读硬盘文件
            return extractTextWithPDFBox(new File(filePath));
        } else {
            // 图片走内存字节
            return ocrService.extractTextFromImage(fileBytes);
        }
    }

    /* ===================== 私有工具 ===================== */
    private String extractTextWithPDFBox(File pdfFile) throws Exception {
        org.apache.pdfbox.pdmodel.PDDocument document = null;
        try {
            document = org.apache.pdfbox.pdmodel.PDDocument.load(pdfFile);
            org.apache.pdfbox.text.PDFTextStripper stripper = new org.apache.pdfbox.text.PDFTextStripper();
            String text = stripper.getText(document);
            if (text.trim().length() < 100) {
                throw new RuntimeException("可能是图片PDF，文本内容太少");
            }
            return text;
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }
}