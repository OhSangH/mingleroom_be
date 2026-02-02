package com.mingleroom.exception;


import com.mingleroom.common.enums.ErrorCode;
import com.mingleroom.exception.dto.ErrorRes;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1) 우리가 던지는 커스텀 예외
    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ErrorRes> handleGlobal(GlobalException e, HttpServletRequest req) {
        var status = e.getErrorCode().status();
        var body = ErrorRes.of(
                status.value(),
                status.getReasonPhrase(),
                e.getMessage(),
                req.getRequestURI(),
                e.getErrorCode().code(),
                null
        );
        return ResponseEntity.status(status).body(body);
    }

    // 2) @Valid 검증 실패 (DTO Validation)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRes> handleValidation(MethodArgumentNotValidException e, HttpServletRequest req) {
        Map<String, Object> details = new HashMap<>();
        Map<String, String> fieldErrors = new HashMap<>();

        for (FieldError fe : e.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        details.put("fieldErrors", fieldErrors);

        HttpStatus status = HttpStatus.BAD_REQUEST;
        var body = ErrorRes.of(
                status.value(),
                status.getReasonPhrase(),
                "요청 값이 올바르지 않습니다.",
                req.getRequestURI(),
                ErrorCode.BAD_REQUEST.code(),
                details
        );
        return ResponseEntity.status(status).body(body);
    }

    // 3) 그 외 모든 예외 (마지막 안전망)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRes> handleAny(Exception e, HttpServletRequest req) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        var body = ErrorRes.of(
                status.value(),
                status.getReasonPhrase(),
                "서버 오류가 발생했습니다.",
                req.getRequestURI(),
                ErrorCode.INTERNAL_ERROR.code(),
                null
        );
        return ResponseEntity.status(status).body(body);
    }
}