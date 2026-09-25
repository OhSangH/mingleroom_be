package com.mingleroom.domain.room.members.service;

import com.mingleroom.common.enums.ErrorCode;
import com.mingleroom.domain.room.members.dto.RoomMemberRes;
import com.mingleroom.domain.room.members.entity.RoomMember;
import com.mingleroom.domain.room.members.repository.RoomMemberRepository;
import com.mingleroom.common.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomMemberService {
    private final RoomMemberRepository roomMemberRepository;

    @Transactional(readOnly = true)
    public List<RoomMemberRes> getRoomMember(Long roomId, Long userId){
        if (!roomMemberRepository.existsByIdRoomIdAndIdUserId(roomId, userId)) {
            throw new GlobalException(ErrorCode.FORBIDDEN, "방 참가자만 목록을 조회할 수 있습니다.");
        }
        List<RoomMember> roomMembers = roomMemberRepository.findByRoomId(roomId);
        if (roomMembers.isEmpty()) {
            throw new GlobalException(ErrorCode.BAD_REQUEST, "Room Member Not Found");
        }
        return roomMembers.stream()
                .map(m -> new RoomMemberRes(
                        m.getUser().getId(),
                        m.getUser().getEmail(),
                        m.getUser().getUsername(),
                        m.getRoleInRoom(),
                        m.getJoinedAt(),
                        m.getLastSeenAt(),
                        m.isMuted(),
                        m.isHandRaised()
                ))
                .toList();
    }
}
