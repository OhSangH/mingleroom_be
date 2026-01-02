package com.mingleroom.security.jwt;

import com.mingleroom.common.enums.ErrorCode;
import com.mingleroom.exeption.ErrorResWriter;
import com.mingleroom.exeption.dto.ErrorRes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ErrorResWriter writer;

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res,
                       AccessDeniedException ex) throws IOException {
        ErrorRes body = ErrorRes.of(
                ErrorCode.FORBIDDEN.status().value(),
                ErrorCode.FORBIDDEN.name(),
                "권한이 없습니다.",
                req.getRequestURI(),
                "FORBIDDEN",
                Map.of("exception", ex.getClass().getSimpleName())
        );

        writer.write(res, body);
    }
}