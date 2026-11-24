package com.campus.lab.common;

/**
 * 业务异常：用于向前端提示可读的业务错误信息
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
