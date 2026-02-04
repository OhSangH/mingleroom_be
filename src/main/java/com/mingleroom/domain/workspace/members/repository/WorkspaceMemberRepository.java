package com.mingleroom.domain.workspace.members.repository;

import com.mingleroom.domain.workspace.members.entity.WorkspaceMember;
import com.mingleroom.domain.workspace.members.entity.WorkspaceMemberId;
import com.mingleroom.domain.workspace.workspaces.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember,Long> {
    List<WorkspaceMember> findAllByWorkspace(Workspace workspace);

    boolean existsByIdUserIdAndIdWorkspaceId(Long idUserId, Long idWorkspaceId);

    Optional<WorkspaceMember> findById(WorkspaceMemberId workspaceMemberId);
}
