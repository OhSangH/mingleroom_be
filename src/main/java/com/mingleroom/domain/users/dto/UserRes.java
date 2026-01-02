package com.mingleroom.domain.users.dto;

public record UserRes(Long id, String email, String username, String role,String profileImg, boolean isBanned) {
}
