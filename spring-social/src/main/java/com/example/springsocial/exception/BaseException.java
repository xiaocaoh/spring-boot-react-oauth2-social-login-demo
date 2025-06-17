package com.example.springsocial.exception;

import com.example.springsocial.util.IStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 异常基类
 * </p>
 *
 * @author yangcc
 * @date Created in 2020-12-07 14:57
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BaseException extends RuntimeException {
    private static final long serialVersionUID = -3425547673848952744L;

    private Integer code;
    private String type;
    private IStatus status;
    private String message;

    public BaseException(IStatus status) {
        super(status.getMessage());
        this.status = status;
        this.code = status.getCode();
        this.type = status.getType();
        this.message = status.getMessage();
    }

    public BaseException(IStatus status, String message) {
        this(status);
        this.message = message;
    }

    public IStatus getStatus() {
        return this.status;
    }
}