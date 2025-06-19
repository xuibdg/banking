package com.core.banking.controller;

import com.core.banking.utils.exception.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public abstract class BaseCRUDController {

    public static BaseResponse buildSuccessResponse(Object data) {
        return BaseResponse.builder()
                .httpStatus(HttpStatus.OK)
                .status(0)
                .message("ok")
                .data(data)
                .build();
    }

    public static <T> BaseResponse<T> buildCreatedResponse(T data) {
        return BaseResponse.<T>builder()
                .httpStatus(HttpStatus.CREATED)
                .status(0)
                .message("created successfully")
                .data(data)
                .build();

    }
}

