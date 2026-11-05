package com.jianlidb.resume.mapper;

import com.jianlidb.resume.model.entity.Resume;
import com.jianlidb.resume.model.vo.ResumeVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ResumeMapper {

    @Insert("INSERT INTO resumes (user_id, original_filename, file_path, file_size, raw_text, status, created_at) " +
            "VALUES (#{userId}, #{originalFilename}, #{filePath}, #{fileSize}, #{rawText}, #{status}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Resume resume);

    @Select("SELECT id, user_id, original_filename, name, phone, email, gender, age, " +
            "skills, education, work_experience, status, created_at " +
            "FROM resumes WHERE id = #{id}")
    ResumeVO selectResumeVOById(Long id);

    // 使用XML方式实现的查询方法
    List<ResumeVO> searchBySkillXml(@Param("keyword") String keyword);

    List<ResumeVO> searchBySkill(@Param("keyword") String keyword);

    List<ResumeVO> advancedSearch(@Param("keyword") String keyword,
                                  @Param("gender") String gender,
                                  @Param("minAge") Integer minAge,
                                  @Param("maxAge") Integer maxAge,
                                  @Param("sortField") String sortField,
                                  @Param("sortOrder") String sortOrder,
                                  @Param("offset") Integer offset,
                                  @Param("pageSize") Integer pageSize);

    int countSearchResults(@Param("keyword") String keyword);

    int countAdvancedSearchResults(@Param("keyword") String keyword,
                                   @Param("gender") String gender,
                                   @Param("minAge") Integer minAge,
                                   @Param("maxAge") Integer maxAge);

    @Update("UPDATE resumes SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("UPDATE resumes SET name = #{name}, phone = #{phone}, email = #{email}, " +
            "gender = #{gender}, age = #{age}, " +
            "skills = #{skills}, education = #{education}, work_experience = #{workExperience}, " +
            "parse_result = #{parseResult}, status = #{status}, updated_at = NOW() " +
            "WHERE id = #{id}")
    int updateParseResult(@Param("id") Long id,
                          @Param("name") String name,
                          @Param("phone") String phone,
                          @Param("email") String email,
                          @Param("gender") String gender,
                          @Param("age") Integer age,
                          @Param("skills") String skills,
                          @Param("education") String education,
                          @Param("workExperience") String workExperience,
                          @Param("parseResult") String parseResult,
                          @Param("status") Integer status);
    int deleteAllResumes();
}