package com.jianlidb.resume.service;

import com.jianlidb.resume.model.domain.ResumeInfo;

public interface AIParseService {
    ResumeInfo parseResumeText(String resumeText);
    ResumeInfo parseResumeText(String resumeText, int maxRetries);
}