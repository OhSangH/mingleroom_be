package com.mingleroom.domain.room.members.controller;

import com.mingleroom.domain.room.members.dto.RoomMemberRes;
import com.mingleroom.domain.room.members.service.RoomMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RoomMemberController {
    RoomMemberService roomMemberService;

    @GetMapping("/{roomId}/members")
    @Transactional(readOnly = true)
    public ResponseEntity<List<RoomMemberRes>> getRoomMembers(@PathVariable Long roomId) {
        List<RoomMemberRes> roomMemberRes = roomMemberService.getRoomMember(roomId);
        return ResponseEntity.ok(roomMemberRes);
    }
}
