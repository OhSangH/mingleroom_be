package com.mingleroom.domain.users.controller;

import com.mingleroom.domain.users.dto.JoinReq;
import com.mingleroom.domain.users.dto.LoginReq;
import com.mingleroom.domain.users.dto.TokenRes;
import com.mingleroom.domain.users.entity.User;
import com.mingleroom.domain.users.service.AuthService;
import com.mingleroom.security.config.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;

    @PostMapping("/join")
    public ResponseEntity<User> join(@Valid @RequestBody JoinReq req){
        authService.join(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<TokenRes> login(@Valid @RequestBody LoginReq req) {

        return ResponseEntity.ok(authService.login(req));
    }

    @GetMapping("/principal")
    public ResponseEntity<UserPrincipal> principalUser(@AuthenticationPrincipal UserPrincipal user){
        log.info("user : {}", user);
        return ResponseEntity.ok(user);
    }

}
