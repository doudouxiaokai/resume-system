package com.jianlidb.resume;

import com.jianlidb.resume.config.BaiduOCRConfig;
import com.jianlidb.resume.config.TywyConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@MapperScan("com.jianlidb.resume.mapper")
public class ResumeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResumeApplication.class, args);
        System.out.println("========================================");
        System.out.println("简历信息智能解析与搜索系统启动成功！");
        System.out.println("访问地址: http://localhost:8080");
        System.out.println("API地址: http://localhost:8080/api");
        System.out.println("========================================");
    }
}