package com.mingleroom.domain.workspace.members.controller;

import com.mingleroom.domain.workspace.members.dto.AddWorkspaceMemberReq;
import com.mingleroom.domain.workspace.members.dto.WorkspaceMemberRes;
import com.mingleroom.domain.workspace.members.service.WorkspaceMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/workspace/{workspaceId}/member")
public class WorkspaceMemberController {

    private final WorkspaceMemberService workspaceMemberService;

    @GetMapping
    public ResponseEntity<List<WorkspaceMemberRes>> getWorkspaceMembers(@PathVariable Long workspaceId) {
        List<WorkspaceMemberRes> workspaceMembers = workspaceMemberService.getWorkspaceMember(workspaceId);
        return ResponseEntity.ok(workspaceMembers);
    }

    @PostMapping
    public ResponseEntity<WorkspaceMemberRes> addWorkspaceMember(@PathVariable Long workspaceId, @RequestBody AddWorkspaceMemberReq workspaceMember) {
        WorkspaceMemberRes newWorkspaceMember = workspaceMemberService.addWorkspaceMember(workspaceId, workspaceMember.email(), workspaceMember.role());
        return ResponseEntity.ok(newWorkspaceMember);
    }

}
