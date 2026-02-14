package com.mingleroom.domain.room.rooms.controller;

import com.mingleroom.common.enums.ErrorCode;
import com.mingleroom.domain.room.rooms.dto.RoomCreateReq;
import com.mingleroom.domain.room.rooms.dto.RoomRes;
import com.mingleroom.domain.room.rooms.service.RoomService;
import com.mingleroom.common.exception.GlobalException;
import com.mingleroom.security.config.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/room")
public class RoomController {

    private final RoomService roomService;

    @PostMapping("/create")
    public ResponseEntity<RoomRes> createRoom(@RequestBody RoomCreateReq roomCreateReq, @AuthenticationPrincipal UserPrincipal principal) {
        log.info("principal {}", principal.getEmail());
        RoomRes roomRes = roomService.createRoom(principal.getEmail(), roomCreateReq);

        if (roomRes == null) {
            throw new GlobalException(ErrorCode.INTERNAL_ERROR, "Room Create Failed");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(roomRes);
    }

    @PostMapping("/{roomId}/join/me")
    public ResponseEntity<Void> joinRoom(@PathVariable Long roomId, @AuthenticationPrincipal UserPrincipal principal) {
        roomService.joinRoom(roomId, principal.getEmail());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{roomId}/leave/me")
    public ResponseEntity<Void> leaveRoom(@PathVariable Long roomId, @AuthenticationPrincipal UserPrincipal principal) {
        roomService.leaveRoom(roomId, principal.getEmail());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
