package com.mingleroom.domain.workspace.members.controller;

import com.mingleroom.common.enums.WorkspaceRole;
import com.mingleroom.domain.workspace.members.dto.AddWorkspaceMemberReq;
import com.mingleroom.domain.workspace.members.dto.WorkspaceMemberRes;
import com.mingleroom.domain.workspace.members.service.WorkspaceMemberService;
import com.mingleroom.security.config.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/workspace/{workspaceId}/member")
public class WorkspaceMemberController {

    private final WorkspaceMemberService workspaceMemberService;

    @GetMapping
    public ResponseEntity<List<WorkspaceMemberRes>> getWorkspaceMembers(@PathVariable Long workspaceId,@AuthenticationPrincipal UserPrincipal principal) {
        List<WorkspaceMemberRes> workspaceMembers = workspaceMemberService.getWorkspaceMember(workspaceId, principal);
        return ResponseEntity.ok(workspaceMembers);
    }

    @PostMapping
    public ResponseEntity<WorkspaceMemberRes> addWorkspaceMember(@PathVariable Long workspaceId, @RequestBody AddWorkspaceMemberReq workspaceMember, @AuthenticationPrincipal UserPrincipal principal) {
        WorkspaceMemberRes newWorkspaceMember = workspaceMemberService.addWorkspaceMember(workspaceId, workspaceMember.email(), workspaceMember.role(), principal);
        return ResponseEntity.ok(newWorkspaceMember);
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<WorkspaceMemberRes> setWorkspaceMemberRole(@PathVariable Long workspaceId, @PathVariable Long userId, @RequestBody WorkspaceRole role, @AuthenticationPrincipal UserPrincipal principal){
        WorkspaceMemberRes workspaceMember = workspaceMemberService.setWorkspaceMemberRole(workspaceId, userId, role, principal);
        return ResponseEntity.ok(workspaceMember);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteWorkspaceMember(@PathVariable Long workspaceId, @PathVariable Long userId, @AuthenticationPrincipal UserPrincipal principal){
        workspaceMemberService.deleteWorkspaceMember(workspaceId, userId, principal);
        return ResponseEntity.noContent().build();
    }

}
