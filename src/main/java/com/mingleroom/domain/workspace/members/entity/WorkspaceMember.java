package com.mingleroom.domain.workspace.members.entity;


import com.mingleroom.domain.enums.WorkspaceRole;
import com.mingleroom.domain.users.entity.User;
import com.mingleroom.domain.workspace.workspaces.entity.Workspace;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@DynamicInsert
@Entity
@Table(name = "workspace_members")
public class WorkspaceMember {

    @EmbeddedId
    private WorkspaceMemberId id;

    @MapsId("workspaceId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_in_workspace", nullable = false, length = 20)
    private WorkspaceRole roleInWorkspace;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;
}