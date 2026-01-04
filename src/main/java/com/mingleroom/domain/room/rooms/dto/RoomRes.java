package com.mingleroom.domain.room.rooms.dto;

import com.mingleroom.common.enums.RoomVisibility;

public record RoomRes(
        Long id,
        String title,
        RoomVisibility visibility
) {
}
