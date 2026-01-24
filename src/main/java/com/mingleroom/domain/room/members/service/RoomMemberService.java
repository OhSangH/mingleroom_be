package com.mingleroom.domain.room.members.service;

import com.mingleroom.common.enums.ErrorCode;
import com.mingleroom.domain.room.members.dto.RoomMemberRes;
import com.mingleroom.domain.room.members.entity.RoomMember;
import com.mingleroom.domain.room.members.repository.RoomMemberRepository;
import com.mingleroom.exeption.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomMemberService {
    RoomMemberRepository roomMemberRepository;

    public List<RoomMemberRes> getRoomMember(Long roomId){
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
