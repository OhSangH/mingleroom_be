package com.mingleroom.security.jwt;

import com.mingleroom.common.enums.ErrorCode;
import com.mingleroom.common.exception.ErrorResWriter;
import com.mingleroom.common.exception.dto.ErrorRes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    private final ErrorResWriter writer;

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException authException) throws IOException{

        String code = (String) req.getAttribute("AUTH_ERROR_CODE");
        String message = (String) req.getAttribute("AUTH_ERROR_MESSAGE");

        if (code == null) {
            code = "UNAUTHORIZED";
            message = "인증이 필요합니다.";
        }

        ErrorRes body = ErrorRes.of(
                ErrorCode.UNAUTHORIZED.status().value(),
                ErrorCode.UNAUTHORIZED.name(),
                message,
                req.getRequestURI(),
                code,
                Map.of("exception", authException.getClass().getSimpleName())
        );

        writer.write(res, body);
    }
}
