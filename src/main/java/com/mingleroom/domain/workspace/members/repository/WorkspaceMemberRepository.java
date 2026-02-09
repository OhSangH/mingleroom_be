package com.mingleroom.domain.workspace.members.repository;

import com.mingleroom.domain.workspace.members.entity.WorkspaceMember;
import com.mingleroom.domain.workspace.members.entity.WorkspaceMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember,Long> {
    List<WorkspaceMember> findAllByWorkspaceId(Long workspaceId);

    boolean existsByIdUserIdAndIdWorkspaceId(Long idUserId, Long idWorkspaceId);

    Optional<WorkspaceMember> findById(WorkspaceMemberId workspaceMemberId);
}
