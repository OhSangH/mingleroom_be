package com.mingleroom.domain.workspace.workspaces.service;

import com.mingleroom.common.enums.ErrorCode;
import com.mingleroom.domain.users.entity.User;
import com.mingleroom.domain.users.repository.UserRepository;
import com.mingleroom.domain.workspace.workspaces.dto.WorkspaceCreateReq;
import com.mingleroom.domain.workspace.workspaces.dto.WorkspaceRes;
import com.mingleroom.domain.workspace.workspaces.entity.Workspace;
import com.mingleroom.domain.workspace.workspaces.repository.WorkspaceRepository;
import com.mingleroom.exeption.GlobalException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    @Transactional
    public WorkspaceRes createWorkspace(WorkspaceCreateReq req , String email) {
        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalException(ErrorCode.UNAUTHORIZED,"USER_NOT_FOUND"));

        Workspace ws = Workspace.builder()
                .name(req.name())
                .owner(owner)
                .build();

        workspaceRepository.save(ws);
        entityManager.flush();
        entityManager.refresh(ws);

        return toWorkspaceRes(ws);
    }

    public List<WorkspaceRes> getWorkspaces(Long userId) {
        List<Workspace> wsList = workspaceRepository.findAllByOwnerId(userId);

        return wsList.stream().map(this::toWorkspaceRes).toList();
    }

    private WorkspaceRes toWorkspaceRes(Workspace ws){
        return new WorkspaceRes(ws.getId(),ws.getName(),ws.getOwner().getId(),ws.getCreatedAt());
    }
}
