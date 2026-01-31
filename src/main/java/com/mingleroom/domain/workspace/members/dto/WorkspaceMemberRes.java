package com.mingleroom.domain.workspace.members.dto;

import com.mingleroom.common.enums.WorkspaceRole;

import java.time.OffsetDateTime;

public record WorkspaceMemberRes(
        Long workspaceId,
        Long userId,
        WorkspaceRole role,
        OffsetDateTime joinedAt
) {
}
