package com.jianlidb.resume.service.impl;

import com.jianlidb.resume.mapper.ResumeMapper;
import com.jianlidb.resume.model.vo.ResumeVO;
import com.jianlidb.resume.service.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private ResumeMapper resumeMapper;

    private static final Logger log = LoggerFactory.getLogger(SearchServiceImpl.class);

    @Override
    public List<ResumeVO> searchBySkill(String keyword, int page, int size) {
        // 参数校验
        if (keyword == null) {
            keyword = "";
        }

        keyword = keyword.trim();
        log.info("搜索技能关键词: {}, 分页参数: page={}, size={}", keyword, page, size);

        // 计算分页偏移量
        int offset = (page - 1) * size;

        // 直接调用Mapper获取分页数据（需要修改Mapper支持分页）
        List<ResumeVO> results = resumeMapper.searchBySkill(keyword);

        // 临时内存分页（等待Mapper修改）
        if (results == null || results.isEmpty()) {
            return new ArrayList<>();
        }

        // 内存分页逻辑（临时）
        int startIndex = offset;
        if (startIndex >= results.size()) {
            return new ArrayList<>();
        }

        int endIndex = Math.min(startIndex + size, results.size());
        return results.subList(startIndex, endIndex);
    }

    @Override
    public int countSearchResults(String keyword) {
        if (keyword == null) {
            keyword = "";
        }

        keyword = keyword.trim();
        return resumeMapper.countSearchResults(keyword);
    }

    @Override
    public List<ResumeVO> advancedSearch(String keyword, String gender, Integer minAge, Integer maxAge,
                                         String sortField, String sortOrder, int page, int size) {
        // 参数校验
        if (keyword == null) {
            keyword = "";
        }
        keyword = keyword.trim();

        // 默认值处理
        if (sortField == null || sortField.isEmpty()) {
            sortField = "createdAt";
        }
        if (sortOrder == null || sortOrder.isEmpty()) {
            sortOrder = "desc";
        }

        log.info("高级搜索: keyword={}, gender={}, minAge={}, maxAge={}, sortField={}, sortOrder={}, page={}, size={}",
                keyword, gender, minAge, maxAge, sortField, sortOrder, page, size);

        // 计算分页偏移量
        int offset = (page - 1) * size;

        // 调用Mapper，传入分页参数
        List<ResumeVO> results = resumeMapper.advancedSearch(keyword, gender, minAge, maxAge,
                sortField, sortOrder, offset, size);

        log.info("高级搜索结果总数: {}", results != null ? results.size() : 0);

        return results != null ? results : new ArrayList<>();
    }

    @Override
    public int countAdvancedSearchResults(String keyword, String gender, Integer minAge, Integer maxAge) {
        if (keyword == null) {
            keyword = "";
        }
        keyword = keyword.trim();

        return resumeMapper.countAdvancedSearchResults(keyword, gender, minAge, maxAge);
    }
}