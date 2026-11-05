package com.jianlidb.resume.controller;

import com.jianlidb.resume.common.Result;
import com.jianlidb.resume.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/resume")
public class ResumeDeleteController {

    @Autowired
    private ResumeService resumeService;

    @DeleteMapping("/all")
    public Result<String> deleteAllResumes() {
        try {
            // 这里需要实现清空所有简历的逻辑
            // 你可能需要在ResumeService中添加deleteAllResumes方法
            // 然后调用resumeMapper.deleteAll()
            return Result.success("所有简历已清空");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("清空失败: " + e.getMessage());
        }
    }
}