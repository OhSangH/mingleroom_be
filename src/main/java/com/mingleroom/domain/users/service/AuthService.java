package com.mingleroom.domain.users.service;

import com.mingleroom.common.enums.ErrorCode;
import com.mingleroom.common.enums.RoleGlobal;
import com.mingleroom.domain.refreshtoken.entity.RefreshToken;
import com.mingleroom.domain.refreshtoken.repository.RefreshTokenRepository;
import com.mingleroom.domain.users.dto.JoinReq;
import com.mingleroom.domain.users.dto.LoginReq;
import com.mingleroom.domain.users.dto.TokenRes;
import com.mingleroom.domain.users.entity.User;
import com.mingleroom.domain.users.repository.UserRepository;
import com.mingleroom.exeption.GlobalException;
import com.mingleroom.security.jwt.JwtProvider;
import com.mingleroom.security.jwt.TokenUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;


    @Value("${app.jwt.refreshExpirationMs}")
    private long refreshExpirationMs;

    // 회원가입
    public void join(JoinReq joinReq) {
        String email = joinReq.email();
        String password = joinReq.password();
        String username = joinReq.username();

        if (email.isBlank() || password.isBlank() || username.isBlank()){
            throw new GlobalException(ErrorCode.BAD_REQUEST, "제대로 입력해주세요.");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            throw new GlobalException(ErrorCode.CONFLICT, "이미 사용 중인 이메일입니다.");
        }
        userRepository.save(
                User.builder()
                        .email(email)
                        .passwordHash(passwordEncoder.encode(password))
                        .username(username)
                        .roleGlobal(RoleGlobal.USER).build()
        );
    }

    public TokenRes login(LoginReq loginReq, HttpServletResponse res){
        User user = userRepository.findByEmail(loginReq.email()).orElseThrow(() -> new GlobalException(ErrorCode.BAD_REQUEST, "이메일을 확인해주세요."));

        if (!passwordEncoder.matches(loginReq.password(), user.getPasswordHash())){
            throw new GlobalException(ErrorCode.BAD_REQUEST, "비밀번호가 일치하지않습니다.");
        }

        String token = jwtProvider.createAccessToken(user.getId(), user.getEmail(), user.getRoleGlobal().name());

        createRefreshToken(res, user);

        user.setLastLoginAt(OffsetDateTime.now());
        userRepository.save(user);

        return new TokenRes(token);
    }

    private void setRefreshCookie(HttpServletResponse res, String refreshRaw) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshRaw)
                .httpOnly(true)
                .secure(false) // 운영 HTTPS면 true
                .sameSite("Lax")
                .path("/auth")
                .maxAge(Duration.ofDays(14))
                .build();
        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @Transactional
    public void logout(HttpServletRequest req, HttpServletResponse res) {
        String refreshRaw = extractRefreshCookie(req);
        if (refreshRaw != null) {
            String hash = TokenUtils.sha256Hex(refreshRaw);
            refreshTokenRepository.findByTokenHash(hash).ifPresent(rt -> rt.setRevokedAt(OffsetDateTime.now()));
        }
        clearRefreshCookie(res);
    }


    @Transactional
    public TokenRes refresh(HttpServletRequest req, HttpServletResponse res) {
        String refreshRaw = extractRefreshCookie(req);
        if (refreshRaw == null || refreshRaw.isBlank()) {
            throw new GlobalException(ErrorCode.UNAUTHORIZED, "Refresh token이 없습니다.");
        }

        String hash = TokenUtils.sha256Hex(refreshRaw);

        RefreshToken saved = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new GlobalException(ErrorCode.UNAUTHORIZED, "Refresh token이 유효하지 않습니다."));

        if (saved.getRevokedAt() != null || saved.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new GlobalException(ErrorCode.UNAUTHORIZED, "Refresh token이 만료되었거나 폐기되었습니다.");
        }

        User user = userRepository.findById(saved.getUserId())
                .orElseThrow(() -> new GlobalException(ErrorCode.UNAUTHORIZED, "User not found"));



        // ✅ rotation: 기존 refresh 폐기
        saved.setRevokedAt(OffsetDateTime.now());

        // ✅ 새 refresh 발급/저장
        createRefreshToken(res, user);

        // 새 access 발급
        String newAccess = jwtProvider.createAccessToken(user.getId(), user.getEmail(), user.getRoleGlobal().name());
        return new TokenRes(newAccess);
    }

    private String extractRefreshCookie(HttpServletRequest req) {
        if (req.getCookies() == null) return null;
        for (Cookie c : req.getCookies()) {
            if ("refreshToken".equals(c.getName())) return c.getValue();
        }
        return null;
    }

    private void clearRefreshCookie(HttpServletResponse res) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)     // 운영 HTTPS면 true
                .sameSite("Lax")
                .path("/auth")     // login 때 설정한 path랑 반드시 동일
                .maxAge(Duration.ZERO)
                .build();
        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void createRefreshToken(HttpServletResponse res, User user) {
        String newRefreshRaw = TokenUtils.newRefreshToken();
        String newRefreshHash = TokenUtils.sha256Hex(newRefreshRaw);

        RefreshToken newRt = new RefreshToken();
        newRt.setUserId(user.getId());
        newRt.setTokenHash(newRefreshHash);
        newRt.setExpiresAt(OffsetDateTime.now().plusSeconds(refreshExpirationMs / 1000));
        refreshTokenRepository.save(newRt);

        // 쿠키 갱신
        setRefreshCookie(res, newRefreshRaw);
    }
}
