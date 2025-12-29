package com.mingleroom.domain.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JoinReq(
        @Email(message = "이메일 형식에 맞지 않습니다.") @NotBlank(message = "이메일은 필수입니다.") String email,
        @NotBlank(message = "비밀번호는 필수입니다.") @Size(min = 8, max = 20 , message = "비밀번호는 8자 이상 20자 이하여야 합니다.") String password,
        @NotBlank(message = "닉네임은 필수입니다.") @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하여야 합니다.") String username) {
}
