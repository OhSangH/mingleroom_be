package com.mingleroom.domain.users.dto;

import java.time.OffsetDateTime;

public record PrincipalRes(
        Long id,
        String email,
        String username,
        String role,
        String profileImg,
        boolean isBanned,
        OffsetDateTime createdAt,
        OffsetDateTime lastLoginAt,
        OffsetDateTime passwordUpdatedAt
) {
}
