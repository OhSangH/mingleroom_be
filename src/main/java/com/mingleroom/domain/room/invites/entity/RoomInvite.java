package com.mingleroom.domain.room.invites.entity;


import com.mingleroom.common.global.BaseCreatedEntity;
import com.mingleroom.common.enums.InviteType;
import com.mingleroom.common.enums.RoomRole;
import com.mingleroom.domain.room.rooms.entity.Room;
import com.mingleroom.domain.users.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@DynamicInsert
@Entity
@Table(name = "room_invites",
        indexes = {
                @Index(name = "idx_room_invites_room", columnList = "room_id"),
                @Index(name = "idx_room_invites_expires", columnList = "expires_at")
        }
)
public class RoomInvite extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(nullable = false, unique = true, length = 128)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "invite_type", nullable = false, columnDefinition = "invite_type_t")
    private InviteType inviteType;

    @Column(name = "invite_email", length = 255)
    private String inviteEmail;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @Column(name = "max_uses", nullable = false)
    private Integer maxUses;

    @Column(name = "used_count", nullable = false)
    private Integer usedCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_role_in_room", nullable = false, columnDefinition = "room_role_t")
    private RoomRole defaultRoleInRoom;

    @Column(name = "is_revoked", nullable = false)
    private boolean revoked;
}