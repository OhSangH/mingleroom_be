package com.mingleroom.domain.workspace.workspaces.dto;

import java.time.OffsetDateTime;

public record WorkspaceRes(
        Long id,
        String name,
        Long ownerId,
        OffsetDateTime createdAt
) {
}
