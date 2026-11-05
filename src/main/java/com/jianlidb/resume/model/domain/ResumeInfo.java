package com.jianlidb.resume.model.domain;

import lombok.Data;

import java.util.List;

@Data
public class ResumeInfo {
    private String name;
    private String phone;
    private String email;
    private String gender;  // 新增：性别
    private Integer age;    // 新增：年龄
    private List<String> skills;
    private List<Education> educationList;
    private List<WorkExperience> workExperienceList;

    @Data
    public static class Education {
        private String school;
        private String degree;
        private String major;
        private String period;
    }

    @Data
    public static class WorkExperience {
        private String company;
        private String position;
        private String period;
        private String description;
    }

    public String getSkillsJson() {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(skills);
        } catch (Exception e) {
            return "[]";
        }
    }

    public String getEducationJson() {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(educationList);
        } catch (Exception e) {
            return "[]";
        }
    }

    public String getWorkExperienceJson() {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(workExperienceList);
        } catch (Exception e) {
            return "[]";
        }
    }
}