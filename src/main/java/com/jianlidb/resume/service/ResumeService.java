package com.jianlidb.resume.service;

import com.jianlidb.resume.model.vo.ResumeVO;
import org.springframework.web.multipart.MultipartFile;

public interface ResumeService {
    Long processResumeUpload(MultipartFile file, Long userId);
    ResumeVO getResumeById(Long id);
    void clearAllResumes();
}