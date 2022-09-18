package com.itheima.reggie.common;

/**
 * 自定义异常类
 */
public class DiyException extends RuntimeException {
    public DiyException(String msg) {
        super(msg);
    }
}