package com.mingleroom.domain.room.rooms.dto;

import com.mingleroom.common.enums.InvitePolicy;
import com.mingleroom.common.enums.RoomVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RoomCreateReq(
        @NotBlank @Size(max = 150)
        String title,
        @NotNull RoomVisibility visibility,
        @NotNull InvitePolicy invitePolicy,
        Long workspaceId
) {
}
