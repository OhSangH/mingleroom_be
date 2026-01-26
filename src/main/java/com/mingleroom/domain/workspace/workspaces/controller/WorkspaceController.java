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
    public ResponseEntity<List<WorkspaceRes>> getWorkspaces(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<WorkspaceRes> wsList = workspaceService.getWorkspaces(userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.OK).body(wsList);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<WorkspaceRes> getWorkspaceDetail(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable Long roomId) {
        WorkspaceRes ws = workspaceService.getWorkspaceDetail(roomId, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.OK).body(ws);
    }

    @PatchMapping("/{roomId}")
    public ResponseEntity<Void> setWorkspace(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable Long roomId, @RequestBody WorkspaceCreateReq workspaceCreateReq) {
        workspaceService.setWorkspace(roomId, userPrincipal.getId(), workspaceCreateReq);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteWorkspace(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable Long roomId) {
        workspaceService.deleteWorkspace(roomId, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
