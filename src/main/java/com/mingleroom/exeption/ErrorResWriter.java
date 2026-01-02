package com.mingleroom.exeption;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mingleroom.exeption.dto.ErrorRes;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class ErrorResWriter {

    private final ObjectMapper objectMapper;

    public void write(HttpServletResponse res, ErrorRes body) throws IOException {
        res.setStatus(body.status());
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(res.getWriter(), body);
    }
}