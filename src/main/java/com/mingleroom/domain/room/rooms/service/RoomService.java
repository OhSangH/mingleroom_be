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
import com.mingleroom.domain.room.rooms.dto.RoomUpdateReq;
import com.mingleroom.domain.room.rooms.entity.Room;
import com.mingleroom.domain.room.rooms.repository.RoomRepository;
import com.mingleroom.domain.users.entity.User;
import com.mingleroom.domain.users.repository.UserRepository;
import com.mingleroom.domain.workspace.workspaces.entity.Workspace;
import com.mingleroom.domain.workspace.workspaces.repository.WorkspaceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.mingleroom.common.enums.ErrorCode;
import com.mingleroom.common.enums.RoomVisibility;
import com.mingleroom.common.enums.InvitePolicy;
import com.mingleroom.common.exception.GlobalException;
import java.util.List;

import java.time.OffsetDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final com.mingleroom.domain.collaboration.RoomBanRepository bans;
    private final RoomMemberRepository roomMemberRepository;
    private final RoomEventRepository roomEventRepository;
    private final RoomInviteRepository roomInviteRepository;
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final com.mingleroom.domain.workspace.members.repository.WorkspaceMemberRepository workspaceMembers;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<RoomRes> getMyRooms(Long userId) {
        return roomMemberRepository.findAllByIdUserId(userId).stream()
                .map(RoomMember::getRoom)
                .filter(room -> room.getEndedAt() == null)
                .map(room -> new RoomRes(room.getId(), room.getTitle(), room.getVisibility()))
                .toList();
    }

    @Transactional(readOnly = true)
    public RoomRes getRoom(Long roomId, Long userId) {
        if (!roomMemberRepository.existsByIdRoomIdAndIdUserId(roomId, userId)) {
            throw new GlobalException(ErrorCode.FORBIDDEN, "방 참가자만 조회할 수 있습니다.");
        }
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND, "회의실을 찾을 수 없습니다."));
        if(room.getEndedAt()!=null)throw new GlobalException(ErrorCode.CONFLICT,"종료된 회의실입니다.");
        return new RoomRes(room.getId(), room.getTitle(), room.getVisibility());
    }

    public RoomRes createRoom(String email, RoomCreateReq req) {
        if(req.invitePolicy()!=InvitePolicy.LINK)throw new GlobalException(ErrorCode.BAD_REQUEST,"현재 방 생성은 링크 초대 방식만 지원합니다.");
        if(req.visibility()==RoomVisibility.TEAM && req.workspaceId()==null)throw new GlobalException(ErrorCode.BAD_REQUEST,"팀 회의실에는 워크스페이스가 필요합니다.");
        User me = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("USER_NOT_FOUND"));

        Workspace ws = null;
        if (req.workspaceId() != null) {
            if(!workspaceMembers.existsByIdUserIdAndIdWorkspaceId(me.getId(),req.workspaceId()))throw new GlobalException(ErrorCode.FORBIDDEN,"워크스페이스 참가자만 방을 만들 수 있습니다.");
            ws = workspaceRepository.findById(req.workspaceId()).orElseThrow(() -> new EntityNotFoundException("WORKSPACE_NOT_FOUND"));
        }

        Room room = Room.builder()
                .workspace(ws)
                .host(me)
                .title(req.title().trim())
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

    public RoomRes updateRoom(Long roomId,Long userId,RoomUpdateReq req){
        Room room=roomRepository.lockForBoard(roomId).orElseThrow(()->new GlobalException(ErrorCode.NOT_FOUND,"회의실을 찾을 수 없습니다."));
        var member=roomMemberRepository.findByIdRoomIdAndIdUserId(roomId,userId)
            .orElseThrow(()->new GlobalException(ErrorCode.FORBIDDEN,"호스트만 방 설정을 변경할 수 있습니다."));
        if(member.getRoleInRoom()!=RoomRole.HOST)throw new GlobalException(ErrorCode.FORBIDDEN,"호스트만 방 설정을 변경할 수 있습니다.");
        if(room.getEndedAt()!=null)throw new GlobalException(ErrorCode.CONFLICT,"종료된 회의실은 수정할 수 없습니다.");
        if(!java.util.Objects.equals(room.getTitle(),req.expectedTitle())||room.getVisibility()!=req.expectedVisibility())
            throw new GlobalException(ErrorCode.CONFLICT,"다른 화면에서 방 설정을 수정했습니다. 최신 설정을 불러온 뒤 다시 저장하세요.");
        if((room.getVisibility()==RoomVisibility.TEAM)!=(req.visibility()==RoomVisibility.TEAM))
            throw new GlobalException(ErrorCode.BAD_REQUEST,"팀 회의실의 공개 범위 전환은 지원하지 않습니다.");
        String oldTitle=room.getTitle();RoomVisibility oldVisibility=room.getVisibility();
        room.updateDetails(req.title().trim(),req.visibility());
        var payload=objectMapper.createObjectNode();payload.put("action","ROOM_SETTINGS");
        payload.put("oldTitle",oldTitle);payload.put("title",room.getTitle());
        payload.put("oldVisibility",oldVisibility.name());payload.put("visibility",room.getVisibility().name());
        saveEvent(room,member.getUser(),RoomEventType.REACTION,payload);
        return new RoomRes(room.getId(),room.getTitle(),room.getVisibility());
    }

    public void joinRoom(Long roomId, String myEmail){
        User user = userRepository.findByEmail(myEmail).orElseThrow(() -> new EntityNotFoundException("USER_NOT_FOUND"));
        Room room  = roomRepository.lockForBoard(roomId).orElseThrow(() -> new GlobalException(ErrorCode.NOT_FOUND,"회의실을 찾을 수 없습니다."));

        if(room.getEndedAt() != null){
            throw new GlobalException(ErrorCode.CONFLICT, "종료된 회의실입니다.");
        }

        if(bans.existsById(new RoomMemberId(roomId,user.getId())))throw new GlobalException(ErrorCode.FORBIDDEN,"이 회의실에서 내보내진 계정입니다.");
        if(roomMemberRepository.existsByIdRoomIdAndIdUserId(roomId, user.getId())){
            return;
        }

        if (room.isLocked()) {
            throw new GlobalException(ErrorCode.FORBIDDEN, "잠긴 회의실에는 새로 입장할 수 없습니다.");
        }
        if (room.getVisibility() != RoomVisibility.PUBLIC || room.getInvitePolicy() != InvitePolicy.LINK) {
            throw new GlobalException(ErrorCode.FORBIDDEN, "이 회의실은 유효한 초대 링크가 필요합니다.");
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
        roomRepository.lockForBoard(roomId).orElseThrow(()->new GlobalException(ErrorCode.NOT_FOUND,"회의실을 찾을 수 없습니다."));
        User me = userRepository.findByEmail(myEmail)
                .orElseThrow(() -> new EntityNotFoundException("USER_NOT_FOUND"));

        RoomMember member = roomMemberRepository.findByIdRoomIdAndIdUserId(roomId, me.getId())
                .orElseThrow(() -> new IllegalStateException("NOT_A_MEMBER"));

        if(member.getRoleInRoom()==RoomRole.HOST)throw new GlobalException(ErrorCode.CONFLICT,"호스트는 회의를 종료한 뒤 나갈 수 있습니다.");
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
