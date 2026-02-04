package com.mingleroom.domain.workspace.members.service;

import com.mingleroom.common.enums.ErrorCode;
import com.mingleroom.common.enums.WorkspaceRole;
import com.mingleroom.domain.users.entity.User;
import com.mingleroom.domain.users.repository.UserRepository;
import com.mingleroom.domain.workspace.members.dto.WorkspaceMemberRes;
import com.mingleroom.domain.workspace.members.entity.WorkspaceMember;
import com.mingleroom.domain.workspace.members.entity.WorkspaceMemberId;
import com.mingleroom.domain.workspace.members.repository.WorkspaceMemberRepository;
import com.mingleroom.domain.workspace.workspaces.entity.Workspace;
import com.mingleroom.domain.workspace.workspaces.repository.WorkspaceRepository;
import com.mingleroom.exception.GlobalException;
import com.mingleroom.security.config.UserPrincipal;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkspaceMemberService {

    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    public List<WorkspaceMemberRes> getWorkspaceMember(Long workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("WORKSPACE_NOT_FOUND"));

        return workspaceMemberRepository.findAllByWorkspace(workspace).stream().map(this::toWorkspaceMemberRes).toList();
    }

    @Transactional
    public WorkspaceMemberRes addWorkspaceMember(Long workspaceId, String email, WorkspaceRole role, UserPrincipal admin) {
        checkAdmin(workspaceId, admin);

        return addWorkspaceMember(workspaceId, email, role);
    }

    @Transactional
    public WorkspaceMemberRes addWorkspaceMember(Long workspaceId, String email, WorkspaceRole role) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("WORKSPACE_NOT_FOUND"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("USER_NOT_FOUND"));

        if (workspaceMemberRepository.existsByIdUserIdAndIdWorkspaceId(user.getId(), workspace.getId())) {
            throw new GlobalException(ErrorCode.CONFLICT, "이미 존재하는 멤버입니다.");
        }

        WorkspaceMember workspaceMember = WorkspaceMember.builder()
                .id(new WorkspaceMemberId(workspace.getId(), user.getId()))
                .workspace(workspace)
                .user(user)
                .roleInWorkspace(role)
                .joinedAt(OffsetDateTime.now())
                .build();

        workspaceMemberRepository.save(workspaceMember);

        return toWorkspaceMemberRes(workspaceMember);
    }

    public WorkspaceMemberRes setWorkspaceMemberRole(Long workspaceId, Long userId, WorkspaceRole role, UserPrincipal admin) {
        checkAdmin(workspaceId, admin);

        WorkspaceMember workspaceMember = workspaceMemberRepository.findById(new WorkspaceMemberId(workspaceId, userId))
                .orElseThrow(() -> new EntityNotFoundException("WORKSPACE_MEMBER_NOT_FOUND"));

        if(workspaceMember.getRoleInWorkspace().equals(WorkspaceRole.OWNER)) throw new GlobalException(ErrorCode.FORBIDDEN, "OWNER_CANNOT_CHANGE_ROLE");

        workspaceMember.setRoleInWorkspace(role);
        workspaceMemberRepository.save(workspaceMember);

        return toWorkspaceMemberRes(workspaceMember);
    }

    public void deleteWorkspaceMember(Long workspaceId, Long userId, UserPrincipal admin) {
        checkAdmin(workspaceId, admin);

        WorkspaceMember workspaceMember = workspaceMemberRepository.findById(new WorkspaceMemberId(workspaceId, userId))
                .orElseThrow(() -> new EntityNotFoundException("WORKSPACE_MEMBER_NOT_FOUND"));

        if (workspaceMember.getRoleInWorkspace().equals(WorkspaceRole.OWNER)) {
            throw new GlobalException(ErrorCode.FORBIDDEN, "OWNER_CANNOT_DELETE");
        }

        workspaceMemberRepository.delete(workspaceMember);
    }

    public void checkAdmin(Long workspaceId, UserPrincipal admin) {
        WorkspaceMember workspaceAdmin = workspaceMemberRepository.findById(new WorkspaceMemberId(workspaceId, admin.getId()))
                .orElseThrow(() -> new EntityNotFoundException("WORKSPACE_ADMIN_NOT_FOUND"));

        if (workspaceAdmin.getRoleInWorkspace().equals(WorkspaceRole.MEMBER)) {
            requireGlobalAdmin(admin);
        }
    }

    private void requireGlobalAdmin(UserPrincipal admin) {
        if (!admin.getRole().equals("ROLE_ADMIN")) {
            throw new GlobalException(ErrorCode.FORBIDDEN, "USER_NOT_ADMIN");
        }
    }

    private WorkspaceMemberRes toWorkspaceMemberRes(WorkspaceMember workspaceMember) {
        return new WorkspaceMemberRes(workspaceMember.getId().getWorkspaceId(), workspaceMember.getId().getUserId(), workspaceMember.getRoleInWorkspace(), workspaceMember.getJoinedAt());
    }
}
