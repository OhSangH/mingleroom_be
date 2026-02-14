package com.mingleroom.domain.workspace.workspaces.service;

import com.mingleroom.common.enums.ErrorCode;
import com.mingleroom.domain.users.entity.User;
import com.mingleroom.domain.users.repository.UserRepository;
import com.mingleroom.domain.workspace.members.service.WorkspaceMemberService;
import com.mingleroom.domain.workspace.workspaces.dto.WorkspaceCreateReq;
import com.mingleroom.domain.workspace.workspaces.dto.WorkspaceRes;
import com.mingleroom.domain.workspace.workspaces.entity.Workspace;
import com.mingleroom.domain.workspace.workspaces.repository.WorkspaceRepository;
import com.mingleroom.common.exception.GlobalException;
import com.mingleroom.security.config.UserPrincipal;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberService workspaceMemberService;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    @Transactional
    public WorkspaceRes createWorkspace(WorkspaceCreateReq req, String email) {
        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalException(ErrorCode.UNAUTHORIZED, "USER_NOT_FOUND"));

        Workspace ws = Workspace.builder()
                .name(req.name())
                .owner(owner)
                .build();

        workspaceRepository.save(ws);
        entityManager.flush();
        entityManager.refresh(ws);

        workspaceMemberService.addOwnerOnCreate(ws.getId(), owner);


        return toWorkspaceRes(ws);
    }

    public List<WorkspaceRes> getWorkspaces(Long userId) {
        List<Workspace> wsList = workspaceRepository.findAllByOwnerId(userId);

        return wsList.stream().map(this::toWorkspaceRes).toList();
    }

    public WorkspaceRes getWorkspaceDetail(Long workspaceId, Long userId) {
        Workspace ws = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND, "WORKSPACE_NOT_FOUND"));

        if (!userId.equals(ws.getOwner().getId())) {
            throw new GlobalException(ErrorCode.FORBIDDEN, "USER_NOT_MATCHED");
        }

        return toWorkspaceRes(ws);
    }

    public void setWorkspace(Long workspaceId, Long userId, WorkspaceCreateReq workspaceCreateReq) {
        Workspace ws = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND, "WORKSPACE_NOT_FOUND"));

        if (!userId.equals(ws.getOwner().getId())) {
            throw new GlobalException(ErrorCode.FORBIDDEN, "USER_NOT_MATCHED");
        }
        String name = workspaceCreateReq.name();

        if (name.equals(ws.getName())) throw new GlobalException(ErrorCode.BAD_REQUEST, "NAME_ALREADY_EXISTS");
        ws.setName(name);
        workspaceRepository.save(ws);
    }

    public void deleteWorkspace(Long workspaceId, Long userId) {
        Workspace ws = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND, "WORKSPACE_NOT_FOUND"));
        if (!userId.equals(ws.getOwner().getId())) {
            throw new GlobalException(ErrorCode.FORBIDDEN, "USER_NOT_MATCHED");
        }
        workspaceRepository.delete(ws);
    }

    @Transactional
    public WorkspaceRes transferWorkspaceOwnership(Long workspaceId, Long userId, UserPrincipal reqUser) {
        Workspace ws = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND, "WORKSPACE_NOT_FOUND"));

        Long reqUserId = reqUser.getId();
        Long OwnerId = ws.getOwner().getId();

        if (!reqUserId.equals(OwnerId) && reqUser.getRole().equals("ROLE_USER")) {
            throw new GlobalException(ErrorCode.FORBIDDEN, "USER_NOT_MATCHED");
        }

        if (userId.equals(OwnerId)){
            throw new GlobalException(ErrorCode.NOT_FOUND, "DO_NOT_CHANGE_SELF");
        }

        User newOwner = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(ErrorCode.FORBIDDEN, "USER_NOT_FOUND"));

        workspaceMemberService.transferWorkspaceOwner(workspaceId, userId, OwnerId);

        ws.setOwner(newOwner);

        workspaceRepository.save(ws);

        return  toWorkspaceRes(ws);
    }

    private WorkspaceRes toWorkspaceRes(Workspace ws) {
        return new WorkspaceRes(ws.getId(), ws.getName(), ws.getOwner().getId(), ws.getCreatedAt(), ws.getUpdatedAt());
    }


}
