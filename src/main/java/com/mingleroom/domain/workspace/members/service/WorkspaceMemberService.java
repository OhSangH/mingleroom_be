package com.mingleroom.domain.workspace.members.service;

import com.mingleroom.common.enums.ErrorCode;
import com.mingleroom.domain.users.repository.UserRepository;
import com.mingleroom.domain.workspace.members.entity.WorkspaceMember;
import com.mingleroom.domain.workspace.members.repository.WorkspaceMemberRepository;
import com.mingleroom.domain.workspace.workspaces.repository.WorkspaceRepository;
import com.mingleroom.exeption.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkspaceMemberService {

    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    public List<WorkspaceMember> getWorkspaceMember(Long workspaceId) {
        return workspaceMemberRepository.findAllByWorkspace(workspaceRepository.findById(workspaceId).orElseThrow( () -> new GlobalException(ErrorCode.NOT_FOUND, "워크스페이스를 찾을 수 없습니다.")));
    }


}
