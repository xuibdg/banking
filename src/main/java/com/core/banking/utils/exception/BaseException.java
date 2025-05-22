package com.core.banking.utils.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class BaseException extends RuntimeException {
    protected HttpStatus httpStatus;
    protected String errorCode;
    protected String errorDesc;
    protected String errorMessage;
    protected String rootCause;
    protected Map<String, String> errorMessageMap;

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

    public BaseException(HttpStatus httpStatus, String errorCode, String rootCause, String errorDesc, String errorMessage) {
        super(rootCause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.errorDesc = errorDesc;
        this.errorMessage = errorMessage;
    }

    public BaseException(HttpStatus httpStatus, String errorCode, String rootCause) {
        super(rootCause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    public BaseException(String errorCode, String rootCause) {
        super(rootCause);
        this.errorCode = errorCode;
    }

    public BaseException(HttpStatus httpStatus, String errorCode, String rootCause, Map<String, String> errorMessageMap) {
        super(rootCause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.errorMessageMap = errorMessageMap;
    }

    public void setHttpStatus(final HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public void setErrorCode(final String errorCode) {
        this.errorCode = errorCode;
    }

    public void setErrorDesc(final String errorDesc) {
        this.errorDesc = errorDesc;
    }

    public void setErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setRootCause(final String rootCause) {
        this.rootCause = rootCause;
    }

    public void setErrorMessageMap(final Map<String, String> errorMessageMap) {
        this.errorMessageMap = errorMessageMap;
    }

    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public String getErrorDesc() {
        return this.errorDesc;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public String getRootCause() {
        return this.rootCause;
    }

    public Map<String, String> getErrorMessageMap() {
        return this.errorMessageMap;
    }

    public String toString() {
        HttpStatus var10000 = this.getHttpStatus();
        return "BaseException(httpStatus=" + var10000 + ", errorCode=" + this.getErrorCode() + ", errorDesc=" + this.getErrorDesc() + ", errorMessage=" + this.getErrorMessage() + ", rootCause=" + this.getRootCause() + ", errorMessageMap=" + this.getErrorMessageMap() + ")";
    }
}
