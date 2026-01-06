package com.mingleroom.domain.room.members.entity;

import com.mingleroom.common.enums.RoomRole;
import com.mingleroom.domain.room.rooms.entity.Room;
import com.mingleroom.domain.users.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@DynamicInsert
@Entity
@Table(name = "room_members")
public class RoomMember {

    @EmbeddedId
    private RoomMemberId id;

    @MapsId("roomId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "role_in_room", nullable = false, columnDefinition = "room_role_t")
    private RoomRole roleInRoom;

    @Column(name = "joined_at", nullable = false)
    private OffsetDateTime joinedAt;

    @Column(name = "last_seen_at")
    private OffsetDateTime lastSeenAt;

    @Column(name = "is_muted", nullable = false)
    private boolean muted;

    @Column(name = "hand_raised", nullable = false)
    private boolean handRaised;
}