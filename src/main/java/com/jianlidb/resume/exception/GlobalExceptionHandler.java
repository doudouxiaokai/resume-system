package com.jianlidb.resume.exception;

import com.jianlidb.resume.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 手动创建日志对象，替换 @Slf4j
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(value = BusinessException.class)
    public Result<Object> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(value = Exception.class)
    public Result<Object> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.error(500, "系统异常: " + e.getMessage());
    }

}