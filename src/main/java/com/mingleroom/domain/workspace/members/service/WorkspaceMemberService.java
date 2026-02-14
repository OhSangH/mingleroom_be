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
import com.mingleroom.common.exception.GlobalException;
import com.mingleroom.security.config.UserPrincipal;
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

    public List<WorkspaceMemberRes> getWorkspaceMember(Long workspaceId, UserPrincipal reqUser) {
        requireMemberOrGlobalAdmin(workspaceId, reqUser);

        return workspaceMemberRepository.findAllByWorkspaceId(workspaceId).stream().map(this::toWorkspaceMemberRes).toList();
    }

    @Transactional
    public WorkspaceMemberRes addWorkspaceMember(Long workspaceId, String email, WorkspaceRole role, UserPrincipal reqUser) {
        requireAdminOrGlobal(workspaceId, reqUser);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND, "USER_NOT_FOUND"));

        return addWorkspaceMember(workspaceId, user, role);
    }

    @Transactional
    public void addOwnerOnCreate(Long workspaceId, User owner) {
        addWorkspaceMember(workspaceId, owner, WorkspaceRole.OWNER);
    }

    protected WorkspaceMemberRes addWorkspaceMember(Long workspaceId, User user, WorkspaceRole role) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND, "WORKSPACE_NOT_FOUND"));

        if (workspaceMemberRepository.existsByIdUserIdAndIdWorkspaceId(user.getId(), workspace.getId())) {
            throw new GlobalException(ErrorCode.CONFLICT, "USER_ALREADY_EXISTS");
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

    public WorkspaceMemberRes setWorkspaceMemberRole(Long workspaceId, Long userId, WorkspaceRole role, UserPrincipal reqUser) {
        requireAdminOrGlobal(workspaceId, reqUser);

        WorkspaceMember workspaceMember = workspaceMemberRepository.findById(new WorkspaceMemberId(workspaceId, userId))
                .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND, "WORKSPACE_MEMBER_NOT_FOUND"));

        if (workspaceMember.getRoleInWorkspace().equals(WorkspaceRole.OWNER))
            throw new GlobalException(ErrorCode.FORBIDDEN, "OWNER_ROLE_IMMUTABLE");

        if (role.equals(WorkspaceRole.OWNER)) throw new GlobalException(ErrorCode.FORBIDDEN, "USE_TRANSFER_API");

        workspaceMember.setRoleInWorkspace(role);
        workspaceMemberRepository.save(workspaceMember);

        return toWorkspaceMemberRes(workspaceMember);
    }

    public void deleteWorkspaceMember(Long workspaceId, Long userId, UserPrincipal reqUser) {
        requireAdminOrGlobal(workspaceId, reqUser);

        WorkspaceMember workspaceMember = workspaceMemberRepository.findById(new WorkspaceMemberId(workspaceId, userId))
                .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND, "WORKSPACE_MEMBER_NOT_FOUND"));

        if (workspaceMember.getRoleInWorkspace().equals(WorkspaceRole.OWNER)) {
            throw new GlobalException(ErrorCode.FORBIDDEN, "OWNER_CANNOT_DELETE");
        }

        workspaceMemberRepository.delete(workspaceMember);
    }

    public void requireAdminOrGlobal(Long workspaceId, UserPrincipal user) {
        if (requireGlobalAdmin(user)) return;

        WorkspaceMember workspaceAdmin = workspaceMemberRepository.findById(new WorkspaceMemberId(workspaceId, user.getId()))
                .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND, "WORKSPACE_USER_NOT_FOUND"));

        if (workspaceAdmin.getRoleInWorkspace().equals(WorkspaceRole.MEMBER)) {
            throw new GlobalException(ErrorCode.FORBIDDEN, "MEMBER_CANNOT_CHANGE_ROLE");
        }
    }


    public void transferWorkspaceOwner(Long workspaceId, Long userId, Long ownerId) {
        WorkspaceMember newOwner = workspaceMemberRepository.findById(new WorkspaceMemberId(workspaceId, userId))
                .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND, "WORKSPACE_MEMBER_NOT_FOUND"));

        newOwner.setRoleInWorkspace(WorkspaceRole.OWNER);
        workspaceMemberRepository.save(newOwner);

        WorkspaceMember owner = workspaceMemberRepository.findById(new WorkspaceMemberId(workspaceId, ownerId))
                        .orElseThrow(() -> new  GlobalException(ErrorCode.NOT_FOUND, "WORKSPACE_MEMBER_NOT_FOUND"));

        owner.setRoleInWorkspace(WorkspaceRole.ADMIN);

        workspaceMemberRepository.save(owner);
    }

    public void requireMemberOrGlobalAdmin(Long workspaceId, UserPrincipal user) {
        if (requireGlobalAdmin(user)) return;

        workspaceMemberRepository.findById(new WorkspaceMemberId(workspaceId, user.getId()))
                .orElseThrow(() -> new GlobalException(ErrorCode.FORBIDDEN, "WORKSPACE_USER_NOT_FOUND"));
    }

    private boolean requireGlobalAdmin(UserPrincipal admin) {
        return admin.getRole().equals("ROLE_ADMIN");
    }

    private WorkspaceMemberRes toWorkspaceMemberRes(WorkspaceMember workspaceMember) {
        return new WorkspaceMemberRes(workspaceMember.getId().getWorkspaceId(), workspaceMember.getId().getUserId(), workspaceMember.getRoleInWorkspace(), workspaceMember.getJoinedAt());
    }
}
