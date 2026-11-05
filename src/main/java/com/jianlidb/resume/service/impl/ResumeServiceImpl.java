package com.jianlidb.resume.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jianlidb.resume.exception.BusinessException;
import com.jianlidb.resume.mapper.ResumeMapper;
import com.jianlidb.resume.model.domain.ResumeInfo;
import com.jianlidb.resume.model.entity.Resume;
import com.jianlidb.resume.model.vo.ResumeVO;
import com.jianlidb.resume.service.FileProcessingService;
import com.jianlidb.resume.service.ResumeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
public class ResumeServiceImpl implements ResumeService {

    @Autowired
    private ResumeMapper resumeMapper;

    @Autowired
    private FileProcessingService fileProcessingService;

    @Value("${file.upload.path}")
    private String uploadPath;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Long processResumeUpload(MultipartFile file, Long userId) {
        try {
            /* 1. 立即读字节 + 落盘 */
            byte[] fileBytes = file.getBytes();          // 临时文件还在
            String savedPath = saveUploadedFile(file);   // 备份到 ./uploads/

            /* 2. 创建简历记录 */
            Resume resume = new Resume();
            resume.setUserId(userId);
            resume.setOriginalFilename(file.getOriginalFilename());
            resume.setFilePath(savedPath);
            resume.setFileSize(file.getSize());
            resume.setRawText("");
            resume.setStatus(1);                     // 处理中
            resume.setCreatedAt(LocalDateTime.now());

            resumeMapper.insert(resume);
            log.info("简历记录创建成功，ID: {}", resume.getId());

            /* 3. 异步处理（只传字节与路径，不再传 MultipartFile） */
            Long resumeId = resume.getId();
            new Thread(() -> processResumeAsync(resumeId, fileBytes, savedPath)).start();

            return resumeId;

        } catch (Exception e) {
            log.error("简历处理失败", e);
            throw new BusinessException("简历处理失败: " + e.getMessage());
        }
    }

    /**
     * 异步解析
     * @param resumeId  简历主键
     * @param fileBytes 文件字节数组（OCR 用）
     * @param filePath  硬盘完整路径（如需二次读取）
     */
    private void processResumeAsync(Long resumeId, byte[] fileBytes, String filePath) {
        try {
            resumeMapper.updateStatus(resumeId, 1);

            /* 直接用内存字节做 OCR，不再 new File() */
            ResumeInfo resumeInfo = fileProcessingService.processFile(fileBytes, filePath);

            String rawText = fileProcessingService.extractTextFromFile(fileBytes, filePath);

            Resume resume = new Resume();
            resume.setId(resumeId);
            resume.setName(resumeInfo.getName());
            resume.setPhone(resumeInfo.getPhone());
            resume.setEmail(resumeInfo.getEmail());

            // 设置性别和年龄（从AI解析结果中获取）
            resume.setGender(resumeInfo.getGender());
            resume.setAge(resumeInfo.getAge());

            resume.setSkills(resumeInfo.getSkillsJson());
            resume.setEducation(resumeInfo.getEducationJson());
            resume.setWorkExperience(resumeInfo.getWorkExperienceJson());
            resume.setRawText(rawText);
            resume.setParseResult(objectMapper.writeValueAsString(resumeInfo));
            resume.setStatus(2);   // 成功

            // 修复：正确传递所有参数，包括性别和年龄
            resumeMapper.updateParseResult(
                    resume.getId(),
                    resume.getName(),
                    resume.getPhone(),
                    resume.getEmail(),
                    resume.getGender(),    // 新增：性别
                    resume.getAge(),       // 新增：年龄
                    resume.getSkills(),
                    resume.getEducation(),
                    resume.getWorkExperience(),
                    resume.getParseResult(),
                    resume.getStatus()
            );

            log.info("简历解析成功，ID: {}, 姓名: {}, 性别: {}, 年龄: {}", resumeId, resumeInfo.getName(), resumeInfo.getGender(), resumeInfo.getAge());

        } catch (Exception e) {
            log.error("异步处理简历失败，ID: {}", resumeId, e);
            resumeMapper.updateStatus(resumeId, 3);
        }
    }

    private String saveUploadedFile(MultipartFile file) throws IOException {
        // 创建上传目录
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String uniqueFilename = System.currentTimeMillis() + "_" +
                java.util.UUID.randomUUID().toString() +
                fileExtension;

        Path filePath = uploadDir.resolve(uniqueFilename);

        // 保存文件
        Files.copy(file.getInputStream(), filePath);

        log.info("文件保存成功: {}", filePath.toString());

        return filePath.toString();
    }

    @Override
    public ResumeVO getResumeById(Long id) {
        ResumeVO resume = resumeMapper.selectResumeVOById(id);
        if (resume == null) {
            throw new BusinessException("简历不存在");
        }
        return resume;
    }

    // 新增：清空所有简历
    @Override
    public void clearAllResumes() {
        resumeMapper.deleteAllResumes();
    }
}