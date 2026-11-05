package com.jianlidb.resume.common;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    FAILED(500, "操作失败"),
    FILE_EMPTY(1001, "文件不能为空"),
    FILE_TYPE_ERROR(1002, "文件类型错误"),
    FILE_SIZE_EXCEED(1003, "文件大小超出限制"),
    OCR_FAILED(1004, "OCR识别失败"),
    AI_PARSE_FAILED(1005, "AI解析失败"),
    RESUME_NOT_FOUND(1006, "简历不存在");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}