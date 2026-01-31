package com.mingleroom.domain.workspace.members.dto;

import com.mingleroom.common.enums.WorkspaceRole;

public record AddWorkspaceMemberReq(
        String email,
        WorkspaceRole role
) {
}
