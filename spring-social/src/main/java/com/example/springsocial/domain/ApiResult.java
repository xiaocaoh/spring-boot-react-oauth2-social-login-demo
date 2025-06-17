package com.example.springsocial.domain;

import com.example.springsocial.util.Status;
import com.example.springsocial.exception.BaseException;
import com.example.springsocial.util.IStatus;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 通用的 API 接口封装
 * </p>
 *
 * @author yangcc
 * @date Created in 2020-12-07 14:55
 */
@Data
public class ApiResult<T> implements Serializable {
    private static final long serialVersionUID = 8993485788201922830L;

    /**
     * 成功与否
     */
    private boolean success;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 响应类型
     */
    private String type;

    /**
     * 返回内容
     */
    private String message;

    /**
     * 警告信息
     */
    private String warning;

    /**
     * 开发使用的消息
     */
    private String devMessage;

    /**
     * 返回数据
     */
    private T data;

    public static <T> ApiResult<T> newInstance() {
        return new ApiResult<>();
    }

    public static <T> ApiResult<T> newInstance(boolean success) {
        return new ApiResult<T>().success(success);
    }

    public static <T> ApiResult<T> success() {
        return new ApiResult<T>().succeed();
    }

    public static <T> ApiResult<T> failure() {
        return new ApiResult<T>().failed();
    }

    public static <T> ApiResult<T> error() {
        return new ApiResult<T>().failed().status(Status.ERROR);
    }

    public static <T> ApiResult<T> exception(BaseException exception) {
        return new ApiResult<T>().failed().status(exception.getStatus()).message(exception.getMessage());
    }

    public ApiResult() {
        this.code = Status.CODE_UNKNOWN.getCode();
        this.type = Status.CODE_UNKNOWN.getType();
    }

    public ApiResult<T> succeed() {
        this.success = true;
        this.code = Status.SUCCESS.getCode();
        this.type = Status.SUCCESS.getType();
        return this;
    }

    public ApiResult<T> failed() {
        this.success = false;
        this.code = Status.CODE_UNKNOWN.getCode();
        this.type = Status.CODE_UNKNOWN.getType();
        return this;
    }

    public ApiResult<T> success(boolean success) {
        return success ? succeed() : failed();
    }

    public ApiResult<T> status(IStatus status) {
        this.code = status.getCode();
        this.type = status.getType();
        this.message = status.getMessage();
        return this;
    }

    public ApiResult<T> data(T data) {
        this.data = data;
        return this;
    }

    public ApiResult<T> message(String message) {
        this.message = message;
        return this;
    }

    public ApiResult<T> warning(String message) {
        this.warning = message;
        return this;
    }

    public ApiResult<T> devMessage(String devMessage) {
        this.devMessage = devMessage;
        return this;
    }

    public boolean getSuccess() {
        return success;
    }

    public Integer code() {
        return code;
    }

    public T data() {
        return data;
    }
}