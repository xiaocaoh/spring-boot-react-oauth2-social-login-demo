package com.example.springsocial.util;

/**
 * <p>
 * REST API 错误码接口
 * </p>
 *
 * @author yangcc
 * @date Created in 2018-12-07 14:35
 */
public interface IStatus {
    /*响应类型值*/
    String success = "success";
    String warning = "warning";
    String error = "error";

    /**
     * 状态码
     *
     * @return 状态码
     */
    Integer getCode();

    /**
     * 响应类型
     *
     * @return 响应类型
     */
    String getType();

    /**
     * 返回信息
     *
     * @return 返回信息
     */
    String getMessage();
}