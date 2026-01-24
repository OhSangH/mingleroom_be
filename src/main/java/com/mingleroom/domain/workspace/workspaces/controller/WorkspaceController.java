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
        return  ResponseEntity.status(HttpStatus.CREATED).body(ws);
    }

    @GetMapping
    public  ResponseEntity<List<WorkspaceRes>> getWorkspace(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<WorkspaceRes> wsList = workspaceService.getWorkspaces(userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.OK).body(wsList);
    }


}
