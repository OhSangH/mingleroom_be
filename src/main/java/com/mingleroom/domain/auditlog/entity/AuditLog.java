package com.mingleroom.domain.auditlog.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.mingleroom.common.global.BaseCreatedEntity;
import com.mingleroom.domain.room.rooms.entity.Room;
import com.mingleroom.domain.users.entity.User;
import com.mingleroom.domain.workspace.workspaces.entity.Workspace;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@DynamicInsert
@Entity
@Table(name = "audit_logs",
        indexes = {
                @Index(name = "idx_audit_workspace_time", columnList = "workspace_id, created_at"),
                @Index(name = "idx_audit_room_time", columnList = "room_id, created_at"),
                @Index(name = "idx_audit_actor_time", columnList = "actor_id, created_at")
        }
)
public class AuditLog extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(nullable = false, length = 80)
    private String action;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode metadata;

    @Column(length = 64)
    private String ip;

    @Column(name = "user_agent", length = 255)
    private String userAgent;
}