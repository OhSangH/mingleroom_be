package com.mingleroom.domain.room.members.controller;

import com.mingleroom.domain.room.members.dto.RoomMemberRes;
import com.mingleroom.domain.room.members.service.RoomMemberService;
import com.mingleroom.security.config.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class RoomMemberController {
    private final RoomMemberService roomMemberService;

    // Keep the original route as an alias for existing clients.
    @GetMapping({"/room/{roomId}/members", "/{roomId}/members"})
    public ResponseEntity<List<RoomMemberRes>> getRoomMembers(
            @PathVariable Long roomId, @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(roomMemberService.getRoomMember(roomId, principal.getId()));
    }
}
