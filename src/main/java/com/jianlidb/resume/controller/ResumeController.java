package com.jianlidb.resume.controller;

import com.jianlidb.resume.common.Result;
import com.jianlidb.resume.model.vo.ResumeVO;
import com.jianlidb.resume.service.ResumeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

@Validated
@RestController
@RequestMapping("/resume")
public class ResumeController {

    private static final Logger log = LoggerFactory.getLogger(ResumeController.class);

    @Autowired
    private ResumeService resumeService;

    @PostMapping("/upload")
    public Result<Long> uploadResume(
            @RequestParam("file") @NotNull(message = "文件不能为空") MultipartFile file,
            @RequestParam(value = "userId", required = false) Long userId) {

        // 检查文件类型
        String filename = file.getOriginalFilename();
        if (filename == null) {
            return Result.error("文件名不能为空");
        }

        String lowerFilename = filename.toLowerCase();
        if (!lowerFilename.endsWith(".pdf") &&
                !lowerFilename.endsWith(".jpg") &&
                !lowerFilename.endsWith(".jpeg") &&
                !lowerFilename.endsWith(".png")) {
            return Result.error("仅支持PDF、JPG、PNG格式");
        }

        // 检查文件大小
        if (file.getSize() > 10 * 1024 * 1024) {
            return Result.error("文件大小不能超过10MB");
        }

        try {
            Long resumeId = resumeService.processResumeUpload(file, userId);
            return Result.success("简历上传成功", resumeId);
        } catch (Exception e) {
            log.error("简历上传失败", e);
            return Result.error("简历上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<ResumeVO> getResume(@PathVariable @NotNull(message = "ID不能为空") Long id) {
        try {
            ResumeVO resume = resumeService.getResumeById(id);
            return Result.success(resume);
        } catch (Exception e) {
            log.error("获取简历失败", e);
            return Result.error("获取简历失败");
        }
    }

    // 新增：删除所有简历接口
    @DeleteMapping("/clear-all")
    public Result<Void> clearAllResumes() {
        resumeService.clearAllResumes();
        return Result.success("所有简历已清空", null);
    }
}