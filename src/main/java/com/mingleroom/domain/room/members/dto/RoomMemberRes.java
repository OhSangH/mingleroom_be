package com.mingleroom.domain.room.members.dto;

import com.mingleroom.common.enums.RoomRole;

import java.time.OffsetDateTime;

public record RoomMemberRes(
        Long userId,
        String email,
        String username,
        RoomRole roleInRoom,
        OffsetDateTime joinedAt,
        OffsetDateTime lastSeenAt,
        boolean mute,
        boolean handRaised
) {
}
