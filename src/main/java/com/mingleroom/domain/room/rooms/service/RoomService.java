package com.mingleroom.domain.room.rooms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mingleroom.common.enums.RoomEventType;
import com.mingleroom.common.enums.RoomRole;
import com.mingleroom.domain.room.events.entity.RoomEvent;
import com.mingleroom.domain.room.events.repository.RoomEventRepository;
import com.mingleroom.domain.room.invites.repository.RoomInviteRepository;
import com.mingleroom.domain.room.members.entity.RoomMember;
import com.mingleroom.domain.room.members.entity.RoomMemberId;
import com.mingleroom.domain.room.members.repository.RoomMemberRepository;
import com.mingleroom.domain.room.rooms.dto.RoomCreateReq;
import com.mingleroom.domain.room.rooms.dto.RoomRes;
import com.mingleroom.domain.room.rooms.entity.Room;
import com.mingleroom.domain.room.rooms.repository.RoomRepository;
import com.mingleroom.domain.users.entity.User;
import com.mingleroom.domain.users.repository.UserRepository;
import com.mingleroom.domain.workspace.workspaces.entity.Workspace;
import com.mingleroom.domain.workspace.workspaces.repository.WorkspaceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final RoomEventRepository roomEventRepository;
    private final RoomInviteRepository roomInviteRepository;
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ObjectMapper objectMapper;

    public RoomRes createRoom(String email, RoomCreateReq req) {
        User me = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("USER_NOT_FOUND"));

        Workspace ws = null;
        if (req.workspaceId() != null) {
            ws = workspaceRepository.findById(req.workspaceId()).orElseThrow(() -> new EntityNotFoundException("WORKSPACE_NOT_FOUND"));
        }

        Room room = Room.builder()
                .workspace(ws)
                .host(me)
                .title(req.title())
                .visibility(req.visibility())
                .invitePolicy(req.invitePolicy())
                .locked(false)
                .endedAt(null)
                .build();

        room = roomRepository.save(room);

        RoomMember hostMember = RoomMember.builder()
                .id(new RoomMemberId(room.getId(), me.getId()))
                .room(room)
                .user(me)
                .roleInRoom(RoomRole.HOST)
                .joinedAt(OffsetDateTime.now())
                .lastSeenAt(null)
                .muted(false)
                .handRaised(false)
                .build();

        roomMemberRepository.save(hostMember);

        saveEvent(room,me,RoomEventType.JOIN, payloadJoin("host"));

        return new RoomRes(room.getId(), room.getTitle(), room.getVisibility());

    }

    public void joinRoom(Long roomId, String myEmail){
        User user = userRepository.findByEmail(myEmail).orElseThrow(() -> new EntityNotFoundException("USER_NOT_FOUND"));
        Room room  = roomRepository.findById(roomId).orElseThrow(() -> new EntityNotFoundException("ROOM_NOT_FOUND"));

        if(room.getEndedAt() != null){
            throw new IllegalStateException("ROOM_ENDED");
        }

        if(roomMemberRepository.existsByIdRoomIdAndIdUserId(roomId, user.getId())){
            saveEvent(room,user,RoomEventType.JOIN,payloadJoin("already_member"));
            return;
        }

        RoomMember member = RoomMember.builder()
                .id(new RoomMemberId(roomId, user.getId()))
                .room(room)
                .user(user)
                .roleInRoom(RoomRole.MEMBER)
                .joinedAt(OffsetDateTime.now())
                .lastSeenAt(null)
                .muted(false)
                .handRaised(false)
                .build();

        roomMemberRepository.save(member);
        saveEvent(room,user,RoomEventType.JOIN,payloadJoin("member"));
    }

    public void leaveRoom(Long roomId, String myEmail){
        User me = userRepository.findByEmail(myEmail)
                .orElseThrow(() -> new EntityNotFoundException("USER_NOT_FOUND"));

        RoomMember member = roomMemberRepository.findByIdRoomIdAndIdUserId(roomId, me.getId())
                .orElseThrow(() -> new IllegalStateException("NOT_A_MEMBER"));

        roomMemberRepository.delete(member);

        Room room = member.getRoom();
        saveEvent(room,me,RoomEventType.LEAVE,payloadLeave("room"));
    }

    private void saveEvent(Room room, User actor, RoomEventType type, ObjectNode payload) {
        roomEventRepository.save(
                RoomEvent.builder()
                        .room(room)
                        .actor(actor)
                        .eventType(type)
                        .payload(payload)
                        .build()
        );
    }

    private ObjectNode payloadJoin(String reason) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("reason", reason);
        return node;
    }

    private ObjectNode payloadLeave(String reason) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("reason", reason);
        return node;
    }

}
