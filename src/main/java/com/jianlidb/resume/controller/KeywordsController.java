package com.jianlidb.resume.controller;

import com.jianlidb.resume.common.Result;
import com.jianlidb.resume.service.KeywordsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/keywords")
public class KeywordsController {

    @Autowired
    private KeywordsService keywordsService;

    @GetMapping
    public Result<List<String>> getAllKeywords() {
        try {
            List<String> keywords = keywordsService.extractKeywordsFromDatabase();
            return Result.success(keywords);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取关键词失败: " + e.getMessage());
        }
    }
}