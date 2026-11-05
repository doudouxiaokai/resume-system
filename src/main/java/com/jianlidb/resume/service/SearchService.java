package com.jianlidb.resume.service;

import com.jianlidb.resume.model.vo.ResumeVO;

import java.util.List;

public interface SearchService {
    List<ResumeVO> searchBySkill(String keyword, int page, int size);

    /**
     * 新增：获取搜索结果总数
     */
    int countSearchResults(String keyword);

    /**
     * 新增：高级搜索 - 支持多种筛选和排序
     * @param keyword 关键词
     * @param gender 性别
     * @param minAge 最小年龄
     * @param maxAge 最大年龄
     * @param sortField 排序字段：name（姓名）、age（年龄）、createdAt（上传时间）
     * @param sortOrder 排序方式：asc（升序）、desc（降序）
     * @param page 页码
     * @param size 每页大小
     * @return 搜索结果列表
     */
    List<ResumeVO> advancedSearch(String keyword, String gender, Integer minAge, Integer maxAge,
                                  String sortField, String sortOrder, int page, int size);

    /**
     * 新增：获取高级搜索结果总数
     */
    int countAdvancedSearchResults(String keyword, String gender, Integer minAge, Integer maxAge);
}
