package com.core.banking.utils.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalException extends ResponseEntityExceptionHandler {

    public static final String ROOT_CAUSE = "\n rootCause : ";

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> handleBusinessException(BusinessException ex) {
        log.error("\n errorCode : " + ex.getErrorCode() + "\n errorMessage :" + ex.getErrorMessage());

        ApiError error = ApiError.builder()
                .errors(Collections.singletonList(ex.errorMessage))
                .message(ex.errorMessage)
                .code(ex.errorCode)
                .timestamp(LocalDateTime.now())
                .status(ex.httpStatus)
                .build();

        return ResponseEntityBuilder.build(error);

    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Object> handleIOException(IOException ex) {
        // throw again
        throw new BusinessException(HttpStatus.BAD_REQUEST, ex.getMessage());

    }


    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status, // ✅ Perhatikan: HttpStatusCode, bukan HttpStatus
            WebRequest request) {

        Throwable mostSpecificCause = ex.getMostSpecificCause();

        if (mostSpecificCause instanceof InvalidFormatException) {
            ApiError apiError = new ApiError(
                    LocalDateTime.now(),
                    HttpStatus.BAD_REQUEST,
                    mostSpecificCause.getMessage(),
                    null,
                    null);

            return ResponseEntityBuilder.build(apiError);
        }

        log.error(ROOT_CAUSE, ex);

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST,
                "Malformed JSON request",
                null,
                null);

        return ResponseEntityBuilder.build(apiError);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex) {
        log.error(ROOT_CAUSE, ex);
        List<String> errors = new ArrayList<>();

        if (!Objects.isNull(ex.getConstraintViolations())) {
            errors = ex.getConstraintViolations().stream()
                    .map(err -> err.getMessage())
                    .collect(Collectors.toList());
        }


        return ResponseEntityBuilder.build(
                ApiError.builder()
                        .message("Constraint Violation")
                        .status(HttpStatus.BAD_REQUEST)
                        .errors(errors)
                        .build()
        );
    }


//    @Override
//    protected ResponseEntity<Object> handleMissingServletRequestParameter(
//            MissingServletRequestParameterException ex, HttpHeaders headers,
//            HttpStatus status, WebRequest request) {
//
//        ApiError apiError = new ApiError(LocalDateTime.now(),
//                HttpStatus.BAD_REQUEST,
//                "Missing Parameters",
//                null,
//                Collections.singletonList(ex.getParameterName() + " parameter is missing")
//        );
//
//        return ResponseEntityBuilder.build(apiError);
//    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleAll(Exception ex) {
        log.error(ROOT_CAUSE, ex);

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST,
                "Error occurred",
                null,
                Collections.singletonList(ex.getLocalizedMessage()));

        return ResponseEntityBuilder.build(apiError);

    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.error(ROOT_CAUSE, ex);

        ApiError err = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST,
                "Mismatch Type",
                null,
                null);

        return ResponseEntityBuilder.build(err);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        StringBuilder builder = new StringBuilder();
        builder.append(ex.getContentType());
        builder.append(" media type is not supported. Supported media types are ");
        ex.getSupportedMediaTypes().forEach(t -> builder.append(t).append(", "));

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST,
                "Invalid JSON",
                null,
                Collections.singletonList(builder.toString()));

        return ResponseEntityBuilder.build(apiError);

    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getDefaultMessage())
                .collect(Collectors.toList());
        String message = String.join(", ", errors);
        return ResponseEntityBuilder.build(
                ApiError.builder()
                        .message(message)
                        .status(status)
                        .errors(errors)
                        .build()
        );
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        return ResponseEntityBuilder.build(
                ApiError.builder()
                        .timestamp(LocalDateTime.now())
                        .message("Missing Parameter")
                        .status(HttpStatus.BAD_REQUEST)
                        .errors(Collections.singletonList("Parameter '" + ex.getParameterName() + "' is missing"))
                        .build()
        );
    }
}
