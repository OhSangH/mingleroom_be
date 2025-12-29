package com.mingleroom.exeption.dto;

import java.time.OffsetDateTime;
import java.util.Map;

public record ErrorRes(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        String code,
        Map<String, Object> details
) {
    public static ErrorRes of(int status, String error, String message, String path, String code, Map<String, Object> details) {
        return new ErrorRes(OffsetDateTime.now(), status, error, message, path, code, details);
    }
}


