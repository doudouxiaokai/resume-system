package com.jianlidb.resume.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jianlidb.resume.config.BaiduOCRConfig;
import com.jianlidb.resume.exception.BusinessException;
import com.jianlidb.resume.service.OCRService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@SuppressWarnings("unchecked")
public class BaiDuOCRServiceImpl implements OCRService {

    @Autowired
    private BaiduOCRConfig baiduOCRConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String cachedAccessToken;
    private long tokenExpireTime;

    /* ===================== 对外接口 ===================== */

    @Override
    public String extractTextFromImage(MultipartFile file) throws Exception {
        try { Thread.sleep(3_000); } catch (InterruptedException ignore) {}
        log.info("调用百度OCR识别图片");
        return callBaiduOCR(file.getBytes(), "image", null);
    }

    @Override
    public String extractTextFromPdf(MultipartFile file) throws Exception {
        log.info("调用百度OCR识别PDF");
        String filename = file.getOriginalFilename();
        if (filename != null && filename.toLowerCase().endsWith(".pdf")) {
            return callBaiduOCR(file.getBytes(), "pdf", "pdf");
        } else {
            return extractTextFromImage(file);
        }
    }

    @Override
    public String extractText(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new BusinessException("文件名不能为空");
        }
        String lower = filename.toLowerCase();
        return lower.endsWith(".pdf") ? extractTextFromPdf(file) : extractTextFromImage(file);
    }

    @Override
    public String extractTextFromImage(byte[] imageBytes) throws Exception {
        return callBaiduOCR(imageBytes, "image", null);
    }

    /* ===================== 内部：OCR 请求 ===================== */

    private String callBaiduOCR(byte[] fileBytes, String fileType, String pdfType) throws Exception {
        if (fileBytes == null || fileBytes.length == 0) {
            throw new BusinessException("OCR 输入图片为空");
        }
        String accessToken = getAccessToken();

        // 构造请求体
        Map<String, Object> params = new HashMap<>();
        String base64File = Base64.getEncoder().encodeToString(fileBytes);
        if ("pdf".equals(fileType) && "pdf".equals(pdfType)) {
            params.put("pdf_file", base64File);
            params.put("pdf_file_num", "10");
        } else {
            params.put("image", base64File);   // 百度只认 image 字段
        }
        params.put("language_type", baiduOCRConfig.getLanguageType());
        params.put("detect_direction", baiduOCRConfig.getDetectDirection());
        params.put("paragraph", "true");
        params.put("probability", "true");

        log.info("调用百度OCR，文件类型: {}, 字节: {}, Base64 长度: {}", fileType, fileBytes.length, base64File.length());

        String resp = postToBaiduOCR(baiduOCRConfig.getUrl(), accessToken, params);
        return parseOCRResult(resp);
    }

    /* ===================== Token 获取 ===================== */

    private synchronized String getAccessToken() throws Exception {
        if (cachedAccessToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return cachedAccessToken;
        }
        log.info("获取新的百度OCR Access Token");

        // 使用 URIBuilder 避免手工拼空格
        URIBuilder ub = new URIBuilder("https://aip.baidubce.com/oauth/2.0/token");
        ub.setParameter("grant_type", "client_credentials");
        ub.setParameter("client_id", baiduOCRConfig.getApiKey());
        ub.setParameter("client_secret", baiduOCRConfig.getSecretKey());

        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse resp = client.execute(new HttpPost(ub.build()))) {

            String json = EntityUtils.toString(resp.getEntity());
            Map<String, Object> map = objectMapper.readValue(json, Map.class);

            if (map.containsKey("error")) {
                throw new RuntimeException("获取Access Token失败: " + map.get("error_description"));
            }
            String token = (String) map.get("access_token");
            Integer expires = (Integer) map.get("expires_in");
            this.cachedAccessToken = token;
            this.tokenExpireTime = System.currentTimeMillis() + (expires - 300) * 1000L;
            log.info("获取到新的Access Token，有效期: {} 秒", expires);
            return token;
        }
    }

    /* ===================== POST 请求 ===================== */

    private String postToBaiduOCR(String url, String accessToken, Map<String, Object> params) throws Exception {
        // 构造表单字符串
        StringBuilder form = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (form.length() > 0) form.append("&");
            form.append(entry.getKey()).append("=").append(java.net.URLEncoder.encode(String.valueOf(entry.getValue()), "UTF-8"));
        }

        URIBuilder ub = new URIBuilder(url);
        ub.setParameter("access_token", accessToken);

        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse resp = client.execute(
                     new HttpPost(ub.build()) {{
                         setHeader("Content-Type", "application/x-www-form-urlencoded");
                         setEntity(new StringEntity(form.toString(), "UTF-8"));
                     }})) {

            String respJson = EntityUtils.toString(resp.getEntity());
            if (resp.getStatusLine().getStatusCode() != 200) {
                log.error("百度OCR HTTP 失败: {}", respJson);
                throw new RuntimeException("OCR 请求返回非 200");
            }
            return respJson;
        }
    }

    /* ===================== 结果解析 ===================== */

    private String parseOCRResult(String resultJson) throws Exception {
        Map<String, Object> map = objectMapper.readValue(resultJson, Map.class);

        if (map.containsKey("error_code")) {
            Integer code = (Integer) map.get("error_code");
            String msg = (String) map.get("error_msg");
            log.error("OCR识别失败，错误码: {}, 错误信息: {}", code, msg);
            throw new BusinessException("OCR识别失败: " + msg);
        }

        List<Map<String, String>> wordsResult = (List<Map<String, String>>) map.get("words_result");
        StringBuilder sb = new StringBuilder();
        if (wordsResult != null) {
            for (Map<String, String> w : wordsResult) {
                String words = w.get("words");
                if (words != null && !words.trim().isEmpty()) {
                    sb.append(words).append("\n");
                }
            }
        }
        String text = sb.toString().trim();
        log.info("OCR提取到 {} 字符的文本", text.length());
        if (text.isEmpty()) {
            throw new BusinessException("OCR未提取到任何文字内容");
        }
        return text;
    }
}