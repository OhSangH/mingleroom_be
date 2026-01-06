package com.mingleroom.domain.room.rooms.entity;


import com.mingleroom.common.global.BaseTimeEntity;
import com.mingleroom.common.enums.InvitePolicy;
import com.mingleroom.common.enums.RoomVisibility;
import com.mingleroom.domain.users.entity.User;
import com.mingleroom.domain.workspace.workspaces.entity.Workspace;
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
@Table(name = "rooms")
public class Room extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // workspace_id nullable (ON DELETE SET NULL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @Column(nullable = false, length = 150)
    private String title;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "room_visibility_t")
    private RoomVisibility visibility;

    @Column(name = "room_password_hash", length = 255)
    private String roomPasswordHash;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "invite_policy", nullable = false, columnDefinition = "invite_policy_t")
    private InvitePolicy invitePolicy;

    @Column(name = "is_locked", nullable = false)
    private boolean locked;

    @Column(name = "ended_at")
    private OffsetDateTime endedAt;
}