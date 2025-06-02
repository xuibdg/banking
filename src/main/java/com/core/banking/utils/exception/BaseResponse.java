package com.core.banking.utils.exception;


import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@Builder
public class BaseResponse<T> {

    private HttpStatus httpStatus;

    private String status;

    private String message;

    private T data;
}
