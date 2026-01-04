package com.mingleroom.domain.room.rooms.dto;

import com.mingleroom.common.enums.InvitePolicy;
import com.mingleroom.common.enums.RoomVisibility;
import jakarta.validation.constraints.NotBlank;

public record RoomCreateReq(
        @NotBlank
        String title,
        RoomVisibility visibility,
        InvitePolicy invitePolicy,
        Long workspaceId
) {
}
