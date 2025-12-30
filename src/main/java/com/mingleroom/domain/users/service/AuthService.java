package com.mingleroom.domain.users.service;

import com.mingleroom.common.enums.ErrorCode;
import com.mingleroom.common.enums.RoleGlobal;
import com.mingleroom.domain.users.dto.JoinReq;
import com.mingleroom.domain.users.dto.LoginReq;
import com.mingleroom.domain.users.dto.TokenRes;
import com.mingleroom.domain.users.entity.User;
import com.mingleroom.domain.users.repository.UserRepository;
import com.mingleroom.exeption.GlobalException;
import com.mingleroom.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    // 회원가입
    public void join(JoinReq joinReq) {
        var email = joinReq.email();
        var password = joinReq.password();
        var username = joinReq.username();

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

    public TokenRes login(LoginReq loginReq){
        var user = userRepository.findByEmail(loginReq.email()).orElseThrow(() -> new GlobalException(ErrorCode.BAD_REQUEST, "이메일을 확인해주세요."));

        if (!passwordEncoder.matches(loginReq.password(), user.getPasswordHash())){
            throw new GlobalException(ErrorCode.BAD_REQUEST, "비밀번호가 일치하지않습니다.");
        }

        String token = jwtProvider.createToken(user.getId(), user.getEmail(), user.getRoleGlobal().name());
        return new TokenRes(token);
    }
}
