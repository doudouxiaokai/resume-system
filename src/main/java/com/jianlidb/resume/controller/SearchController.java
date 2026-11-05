package com.jianlidb.resume.controller;

import com.jianlidb.resume.common.Result;
import com.jianlidb.resume.model.vo.ResumeVO;
import com.jianlidb.resume.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping("/skill")
    public Result<Map<String, Object>> searchBySkill(@RequestParam(required = false) String keyword,
                                                     @RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        try {
            // 如果关键词为空，则返回所有简历
            if (keyword == null || keyword.trim().isEmpty()) {
                return getAllResumes(page, size);
            }

            keyword = keyword.trim();

            // 验证分页参数
            if (page < 1) page = 1;
            if (size < 1) size = 20;
            if (size > 100) size = 100; // 限制最大分页大小

            List<ResumeVO> results = searchService.searchBySkill(keyword, page, size);
            int totalCount = searchService.countSearchResults(keyword);

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("keyword", keyword);
            resultMap.put("data", results);
            resultMap.put("total", totalCount);
            resultMap.put("page", page);
            resultMap.put("size", size);
            resultMap.put("totalPages", (int) Math.ceil((double) totalCount / size));

            return Result.success(resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 新增：高级搜索接口
     */
    @GetMapping("/advanced")
    public Result<Map<String, Object>> advancedSearch(@RequestParam(required = false) String keyword,
                                                      @RequestParam(required = false) String gender,
                                                      @RequestParam(required = false) Integer minAge,
                                                      @RequestParam(required = false) Integer maxAge,
                                                      @RequestParam(required = false, defaultValue = "createdAt") String sortField,
                                                      @RequestParam(required = false, defaultValue = "desc") String sortOrder,
                                                      @RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        try {
            // 验证分页参数
            if (page < 1) page = 1;
            if (size < 1) size = 20;
            if (size > 100) size = 100;

            // 验证排序字段
            if (!isValidSortField(sortField)) {
                sortField = "createdAt";
            }
            if (!isValidSortOrder(sortOrder)) {
                sortOrder = "desc";
            }

            List<ResumeVO> results = searchService.advancedSearch(keyword, gender, minAge, maxAge,
                    sortField, sortOrder, page, size);
            int totalCount = searchService.countAdvancedSearchResults(keyword, gender, minAge, maxAge);

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("keyword", keyword);
            resultMap.put("gender", gender);
            resultMap.put("minAge", minAge);
            resultMap.put("maxAge", maxAge);
            resultMap.put("sortField", sortField);
            resultMap.put("sortOrder", sortOrder);
            resultMap.put("data", results);
            resultMap.put("total", totalCount);
            resultMap.put("page", page);
            resultMap.put("size", size);
            resultMap.put("totalPages", (int) Math.ceil((double) totalCount / size));

            return Result.success(resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("高级搜索失败: " + e.getMessage());
        }
    }

    private Result<Map<String, Object>> getAllResumes(int page, int size) {
        try {
            // 使用空字符串搜索所有简历
            List<ResumeVO> results = searchService.searchBySkill("", page, size);
            int totalCount = searchService.countSearchResults("");

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("keyword", "");
            resultMap.put("data", results);
            resultMap.put("total", totalCount);
            resultMap.put("page", page);
            resultMap.put("size", size);
            resultMap.put("totalPages", (int) Math.ceil((double) totalCount / size));

            return Result.success(resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取简历列表失败: " + e.getMessage());
        }
    }

    private Map<String, Object> createEmptyResult() {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("keyword", "");
        resultMap.put("data", new ArrayList<>());
        resultMap.put("total", 0);
        resultMap.put("page", 1);
        resultMap.put("size", 20);
        resultMap.put("totalPages", 0);
        return resultMap;
    }

    private boolean isValidSortField(String sortField) {
        return sortField != null &&
                (sortField.equals("name") || sortField.equals("age") || sortField.equals("createdAt"));
    }

    private boolean isValidSortOrder(String sortOrder) {
        return sortOrder != null &&
                (sortOrder.equals("asc") || sortOrder.equals("desc"));
    }
}