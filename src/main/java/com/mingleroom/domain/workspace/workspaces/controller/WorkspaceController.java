package com.mingleroom.domain.workspace.workspaces.controller;

import com.mingleroom.domain.workspace.workspaces.dto.WorkspaceCreateReq;
import com.mingleroom.domain.workspace.workspaces.dto.WorkspaceRes;
import com.mingleroom.domain.workspace.workspaces.service.WorkspaceService;
import com.mingleroom.security.config.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/workspace")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    public ResponseEntity<WorkspaceRes> createWorkspace(@RequestBody WorkspaceCreateReq workspaceCreateReq, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        WorkspaceRes ws = workspaceService.createWorkspace(workspaceCreateReq, userPrincipal.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(ws);
    }

    @GetMapping
    public ResponseEntity<List<WorkspaceRes>> getWorkspaces(@AuthenticationPrincipal UserPrincipal principal) {
        List<WorkspaceRes> wsList = workspaceService.getWorkspaces(principal.getId());
        return ResponseEntity.status(HttpStatus.OK).body(wsList);
    }

    @GetMapping("/{workspaceId}")
    public ResponseEntity<WorkspaceRes> getWorkspaceDetail(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long workspaceId) {
        WorkspaceRes ws = workspaceService.getWorkspaceDetail(workspaceId, principal.getId());
        return ResponseEntity.status(HttpStatus.OK).body(ws);
    }

    @PatchMapping("/{workspaceId}")
    public ResponseEntity<Void> setWorkspace(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long workspaceId, @RequestBody WorkspaceCreateReq workspaceCreateReq) {
        workspaceService.setWorkspace(workspaceId, principal.getId(), workspaceCreateReq);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{workspaceId}")
    public ResponseEntity<Void> deleteWorkspace(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long workspaceId) {
        workspaceService.deleteWorkspace(workspaceId, principal.getId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{workspaceId}/transfer-ownership")
    public ResponseEntity<WorkspaceRes> transferOwnership(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long workspaceId, @RequestBody Long newOwnershipId) {
        WorkspaceRes ws = workspaceService.transferWorkspaceOwnership(workspaceId, newOwnershipId, principal);
        return ResponseEntity.status(HttpStatus.OK).body(ws);
    }
}
