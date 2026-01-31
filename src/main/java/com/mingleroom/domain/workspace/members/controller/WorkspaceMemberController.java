package com.mingleroom.domain.workspace.members.controller;

import com.mingleroom.domain.workspace.members.dto.AddWorkspaceMemberReq;
import com.mingleroom.domain.workspace.members.entity.WorkspaceMember;
import com.mingleroom.domain.workspace.members.service.WorkspaceMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/workspaces/{workspaceId}/members")
public class WorkspaceMemberController {

    private  final WorkspaceMemberService workspaceMemberService;

    @GetMapping
    public ResponseEntity<List<WorkspaceMember>> getWorkspaceMembers(@PathVariable Long workspaceId){
        List<WorkspaceMember> workspaceMembers = workspaceMemberService.getWorkspaceMember(workspaceId);
        return ResponseEntity.ok(workspaceMembers);
    }

    @PostMapping
    public ResponseEntity<WorkspaceMember> addWorkspaceMember(@PathVariable Long workspaceId, @RequestBody AddWorkspaceMemberReq workspaceMember){
        WorkspaceMember newWorkspaceMember = workspaceMemberService.addWorkspaceMember(workspaceId, workspaceMember.email(), workspaceMember.role());
        return ResponseEntity.ok(newWorkspaceMember);
    }

}
