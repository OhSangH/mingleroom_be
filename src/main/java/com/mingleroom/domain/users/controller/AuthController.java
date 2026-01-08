package com.mingleroom.domain.users.controller;

import com.mingleroom.domain.users.dto.JoinReq;
import com.mingleroom.domain.users.dto.LoginReq;
import com.mingleroom.domain.users.dto.TokenRes;
import com.mingleroom.domain.users.service.AuthService;
import com.mingleroom.security.config.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;

    @PostMapping("/join")
    public ResponseEntity<Void> join(@Valid @RequestBody JoinReq req){
        authService.join(req);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<TokenRes> login(@Valid @RequestBody LoginReq req, HttpServletResponse res) {
        var result = authService.login(req, res);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/principal")
    public ResponseEntity<UserPrincipal> principalUser(@AuthenticationPrincipal UserPrincipal user){

        return ResponseEntity.ok(user);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenRes> refresh(@AuthenticationPrincipal UserPrincipal user, HttpServletRequest req, HttpServletResponse res) {
        return ResponseEntity.ok(authService.refresh(user ,req, res));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest req, HttpServletResponse res) {
        authService.logout(req, res);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
