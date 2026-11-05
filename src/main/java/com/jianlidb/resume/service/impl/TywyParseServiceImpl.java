package com.jianlidb.resume.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jianlidb.resume.config.TywyConfig;
import com.jianlidb.resume.exception.BusinessException;
import com.jianlidb.resume.model.domain.ResumeInfo;
import com.jianlidb.resume.service.AIParseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class TywyParseServiceImpl implements AIParseService {

    @Autowired
    private TywyConfig config;

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public ResumeInfo parseResumeText(String resumeText) {
        return parseResumeText(resumeText, 3);
    }

    @Override
    public ResumeInfo parseResumeText(String resumeText, int maxRetries) {
        int retry = 0;
        Exception lastEx = null;
        while (retry < maxRetries) {
            try {
                return parseWithTywy(resumeText);
            } catch (Exception e) {
                lastEx = e;
                retry++;
                log.warn("通义解析失败，第{}次重试，错误: {}", retry, e.getMessage());
                if (retry < maxRetries) {
                    try { Thread.sleep(1000L * retry); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }
        }
        throw new BusinessException("通义解析失败，已重试" + maxRetries + "次: " + (lastEx != null ? lastEx.getMessage() : "未知错误"));
    }

    private ResumeInfo parseWithTywy(String text) throws Exception {
        String prompt = buildPrompt(text);
        log.debug("发送给通义的Prompt: {}", prompt);
        String resp = callTywy(prompt);
        return parseResponse(resp);
    }

    private String buildPrompt(String resumeText) {
        String truncated = resumeText.length() > 3000 ? resumeText.substring(0, 3000) + "..." : resumeText;
        return "你是专业简历解析助手，请从以下文本提取结构化信息并返回纯JSON，不要有任何说明文字：\n" +
                "{\n\"name\":\"姓名\",\n\"phone\":\"电话\",\n\"email\":\"邮箱\",\n\"gender\":\"性别（男/女/未知）\",\n\"age\":\"年龄（整数，如提取不到则返回null）\",\n\"skills\":[\"技能1\",\"技能2\"],\n" +
                "\"educationList\":[{\"school\":\"学校\",\"degree\":\"学位\",\"major\":\"专业\",\"period\":\"时间\"}],\n" +
                "\"workExperienceList\":[{\"company\":\"公司\",\"position\":\"职位\",\"period\":\"时间\",\"description\":\"描述\"}]\n}\n\n" +
                "注意事项：\n" +
                "1. 性别：如果简历中明确提到性别，则提取为'男'或'女'，否则为'未知'\n" +
                "2. 年龄：如果简历中明确提到年龄或出生年份（可从简历中的年龄、出生日期等信息推断），请提取为整数，否则返回null\n" +
                "3. 技能：提取简历中提到的所有专业技能，至少提取3-5个\n" +
                "\n" +
                "简历文本：\n" + truncated;
    }

    /**
     * 修改API调用方式：
     * 1. 将API key放在Authorization请求头中（Bearer token格式）
     * 2. 使用配置的超时设置
     */
    private String callTywy(String prompt) throws Exception {
        // 修正：使用正确的泛型类型 List<Map<String, Object>>
        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        messages.add(message);

        Map<String, Object> body = new HashMap<>();
        body.put("model", config.getModel());
        body.put("messages", messages);

        // 参数设置
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("temperature", config.getTemperature());
        parameters.put("max_tokens", config.getMaxTokens());
        parameters.put("top_p", config.getTopP() != null ? config.getTopP() : 0.85);
        parameters.put("seed", config.getSeed() != null ? config.getSeed() : 1234);
        body.put("parameters", parameters);

        // 创建HTTP客户端和请求配置
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(config.getTimeout() * 1000)
                .setSocketTimeout(config.getTimeout() * 1000)
                .build();

        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build()) {

            HttpPost httpPost = new HttpPost(config.getUrl());

            // 设置请求头 - 将API key放在Authorization头中
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + config.getApiKey());

            // 设置请求体
            httpPost.setEntity(new StringEntity(mapper.writeValueAsString(body), "UTF-8"));

            log.debug("调用通义API，URL: {}, 请求体: {}", config.getUrl(), mapper.writeValueAsString(body));

            try (CloseableHttpResponse resp = client.execute(httpPost)) {
                String json = EntityUtils.toString(resp.getEntity(), "UTF-8");
                int statusCode = resp.getStatusLine().getStatusCode();

                if (statusCode != 200) {
                    log.error("通义API调用失败，状态码: {}, 响应: {}", statusCode, json);
                    throw new RuntimeException("通义API请求失败，状态码: " + statusCode);
                }

                log.debug("通义API响应: {}", json);
                return json;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private ResumeInfo parseResponse(String resp) throws Exception {
        Map<String, Object> root = mapper.readValue(resp, Map.class);

        // 检查是否有错误
        if (root.containsKey("error")) {
            Object errorObj = root.get("error");
            String errorMsg;
            if (errorObj instanceof Map) {
                Map<String, Object> errorMap = (Map<String, Object>) errorObj;
                errorMsg = (String) errorMap.getOrDefault("message", "未知错误");
            } else {
                errorMsg = errorObj.toString();
            }
            throw new RuntimeException("通义API错误: " + errorMsg);
        }

        // 解析响应内容
        List<Map<String, Object>> choices = (List<Map<String, Object>>) root.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("通义API响应格式错误: 没有choices字段");
        }

        Map<String, Object> firstChoice = choices.get(0);
        Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
        if (message == null) {
            throw new RuntimeException("通义API响应格式错误: 没有message字段");
        }

        String content = (String) message.get("content");
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("通义API响应格式错误: 内容为空");
        }

        String jsonStr = extractJsonFromResponse(content);
        log.debug("提取的JSON: {}", jsonStr);

        // 解析为Map以便处理可能的数据类型问题
        Map<String, Object> resumeMap = mapper.readValue(jsonStr, Map.class);

        // 处理性别字段：确保性别值是规范的
        if (resumeMap.containsKey("gender")) {
            Object genderObj = resumeMap.get("gender");
            if (genderObj != null) {
                String gender = genderObj.toString().trim();
                // 规范化性别值
                if (gender.equals("男") || gender.equals("男性") || gender.equals("Male") || gender.equals("male")) {
                    resumeMap.put("gender", "男");
                } else if (gender.equals("女") || gender.equals("女性") || gender.equals("Female") || gender.equals("female")) {
                    resumeMap.put("gender", "女");
                } else {
                    resumeMap.put("gender", "未知");
                }
            } else {
                resumeMap.put("gender", "未知");
            }
        } else {
            resumeMap.put("gender", "未知");
        }

        // 处理年龄字段：确保年龄是整数或null
        if (resumeMap.containsKey("age")) {
            Object ageObj = resumeMap.get("age");
            if (ageObj != null) {
                try {
                    if (ageObj instanceof Integer) {
                        // 已经是整数，直接使用
                        Integer age = (Integer) ageObj;
                        if (age > 0 && age < 100) {
                            resumeMap.put("age", age);
                        } else {
                            resumeMap.put("age", null);
                        }
                    } else if (ageObj instanceof String) {
                        // 字符串类型，尝试提取数字
                        String ageStr = (String) ageObj;
                        Pattern pattern = Pattern.compile("\\d+");
                        Matcher matcher = pattern.matcher(ageStr);
                        if (matcher.find()) {
                            int age = Integer.parseInt(matcher.group());
                            if (age > 0 && age < 100) {
                                resumeMap.put("age", age);
                            } else {
                                resumeMap.put("age", null);
                            }
                        } else {
                            resumeMap.put("age", null);
                        }
                    } else if (ageObj instanceof Double) {
                        // 双精度类型，转换为整数
                        Double ageDouble = (Double) ageObj;
                        int age = ageDouble.intValue();
                        if (age > 0 && age < 100) {
                            resumeMap.put("age", age);
                        } else {
                            resumeMap.put("age", null);
                        }
                    } else {
                        resumeMap.put("age", null);
                    }
                } catch (Exception e) {
                    log.warn("年龄解析失败: {}", e.getMessage());
                    resumeMap.put("age", null);
                }
            } else {
                resumeMap.put("age", null);
            }
        } else {
            resumeMap.put("age", null);
        }

        // 将处理后的Map转换回JSON字符串，然后解析为ResumeInfo对象
        String processedJson = mapper.writeValueAsString(resumeMap);
        return mapper.readValue(processedJson, ResumeInfo.class);
    }

    private String extractJsonFromResponse(String content) {
        int start = content.indexOf("{");
        int end = content.lastIndexOf("}") + 1;
        if (start >= 0 && end > start) {
            return content.substring(start, end);
        }
        return content;
    }
}