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
import com.mingleroom.exeption.GlobalException;
import jakarta.transaction.Transactional;
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
                .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND, "워크스페이스를 찾을 수 없습니다."));

        return workspaceMemberRepository.findAllByWorkspace(workspace).stream().map(this::toWorkspaceMemberRes).toList();
    }

    @Transactional
    public WorkspaceMemberRes addWorkspaceMember(Long workspaceId, String email, WorkspaceRole role) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND, "워크스페이스를 찾을 수 없습니다."));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND, "유저를 찾을 수 없습니다."));

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

    private WorkspaceMemberRes toWorkspaceMemberRes(WorkspaceMember workspaceMember) {
        return new WorkspaceMemberRes(workspaceMember.getId().getWorkspaceId(), workspaceMember.getId().getUserId(), workspaceMember.getRoleInWorkspace(), workspaceMember.getJoinedAt());
    }
}
