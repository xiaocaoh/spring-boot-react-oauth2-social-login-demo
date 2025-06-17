package com.example.springsocial.util;

import lombok.Getter;

/**
 * <p>
 * 通用状态码
 * </p>
 *
 * @author yangcc
 * @date Created in 2020-12-07 14:31
 */
@Getter
public enum Status implements IStatus {
    SUCCESS(0, success, "操作成功！"),
    FAIL(1, warning, "操作失败！"),
    CODE_UNKNOWN(-1, warning, "未指定消息编码！"),

    CREATED(201, warning, "对象创建成功！"),
    ACCEPTED(202, warning, "请求已经被接受！"),
    NO_CONTENT(204, warning, "操作已经执行成功，但是没有返回数据！"),

    MOVED_PERM(301, warning, "资源已被移除！"),
    SEE_OTHER(303, warning, "重定向！"),
    NOT_MODIFIED(304, warning, "资源没有被修改！"),

    PARAM_NOT_NULL(400, warning, "参数不能为空！"),
    BAD_REQUEST(400, warning, "参数错误！"),
    UNAUTHORIZED(401, warning, "未授权！"),
    FORBIDDEN(403, warning, "访问受限，授权过期！"),
    NOT_FOUND(404, warning, "资源，服务未找到！"),
    BAD_METHOD(405, warning, "不允许的http方法！"),
    CONFLICT(409, warning, "资源冲突，或者资源被锁！"),
    UNSUPPORTED_TYPE(415, warning, "不支持的数据，媒体类型！"),

    ERROR(500, error, "系统内部错误！"),
    NOT_IMPLEMENTED(501, warning, "接口未实现！");

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 响应类型
     */
    private String type;

    /**
     * 返回信息
     */
    private String message;

    Status(Integer code, String type, String message) {
        this.code = code;
        this.type = type;
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("CommonStatus:{code=%s, type=%s, message=%s} ", getCode(), getType(), getMessage());
    }
}