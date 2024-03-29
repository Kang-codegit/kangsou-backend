package com.kang.kangso.common;

import java.io.Serializable;
import lombok.Data;


@Data
public class BaseResponse<T> implements Serializable {

    private int code;

    private T data;

    private String message;

    // 开发者 [coder_yupi](https://github.com/liyupi)

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}
