package com.jianlidb.resume.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jianlidb.resume.mapper.ResumeMapper;
import com.jianlidb.resume.model.vo.ResumeVO;
import com.jianlidb.resume.service.KeywordsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class KeywordsServiceImpl implements KeywordsService {

    @Autowired
    private ResumeMapper resumeMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<String> extractKeywordsFromDatabase() {
        try {
            Set<String> keywordsSet = new HashSet<>();

            // 获取所有已解析的简历 - 修改调用方式
            List<ResumeVO> allResumes = getAllParsedResumes();

            for (ResumeVO resume : allResumes) {
                if (resume.getSkills() != null && !resume.getSkills().isEmpty()) {
                    try {
                        // 尝试解析JSON格式的技能
                        List<String> skills = objectMapper.readValue(
                                resume.getSkills(),
                                new TypeReference<List<String>>() {}
                        );

                        skills.stream()
                                .filter(skill -> skill != null && !skill.trim().isEmpty())
                                .map(String::trim)
                                .filter(skill -> skill.length() >= 2 && skill.length() <= 20)
                                .forEach(keywordsSet::add);

                    } catch (Exception e) {
                        // 如果不是JSON格式，尝试用逗号分割
                        String[] skills = resume.getSkills().split("[,，、;；]");
                        Arrays.stream(skills)
                                .map(String::trim)
                                .filter(skill -> !skill.isEmpty() && skill.length() >= 2 && skill.length() <= 20)
                                .forEach(keywordsSet::add);
                    }
                }
            }

            // 如果没有提取到关键词，返回默认关键词
            if (keywordsSet.isEmpty()) {
                return Arrays.asList(
                        "Java", "Python", "JavaScript", "Spring", "Vue",
                        "React", "MySQL", "Redis", "Docker", "Linux"
                );
            }

            // 转换为列表并排序
            List<String> result = new ArrayList<>(keywordsSet);
            Collections.sort(result);

            log.info("提取到 {} 个关键词", result.size());
            return result;

        } catch (Exception e) {
            log.error("提取关键词失败", e);
            return Arrays.asList(
                    "Java", "Python", "JavaScript", "Spring", "Vue",
                    "React", "MySQL", "Redis", "Docker", "Linux"
            );
        }
    }

    // 新增方法：获取所有已解析的简历
    private List<ResumeVO> getAllParsedResumes() {
        // 方法1：使用现有的 searchBySkill 方法，传入空字符串获取所有
        // 但是注意：searchBySkill 只查询 status=2 的简历，这正好符合需求
        return resumeMapper.searchBySkill("");

        // 方法2：如果你需要在Mapper中添加一个专门的方法
        // return resumeMapper.getAllParsedResumes();
    }
}