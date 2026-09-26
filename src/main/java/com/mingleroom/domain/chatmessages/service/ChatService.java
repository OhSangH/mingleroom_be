package com.mingleroom.domain.chatmessages.service;
import com.mingleroom.common.enums.*;
import com.mingleroom.common.exception.GlobalException;
import com.mingleroom.domain.chatmessages.dto.*;
import com.mingleroom.domain.chatmessages.entity.ChatMessage;
import com.mingleroom.domain.chatmessages.repository.ChatMessageRepository;
import com.mingleroom.domain.room.members.entity.RoomMember;
import com.mingleroom.domain.room.members.repository.RoomMemberRepository;
import com.mingleroom.domain.room.rooms.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatMessageRepository messages;
    private final RoomMemberRepository members;
    private final RoomRepository rooms;
    private RoomMember member(Long roomId,Long userId){
        return members.findByIdRoomIdAndIdUserId(roomId,userId)
            .orElseThrow(()->new GlobalException(ErrorCode.FORBIDDEN,"방 참가자만 대화를 볼 수 있습니다."));
    }
    @Transactional
    public StoredChatMessage send(Long roomId,Long userId,ChatSendReq req){
        if(req==null||req.type()!=MessageType.TEXT||req.message()==null||req.message().isBlank()||req.message().length()>2000)
            throw new IllegalArgumentException("텍스트 메시지는 1~2000자여야 합니다.");
        if(req.clientMessageId()!=null&&!req.clientMessageId().matches("[a-zA-Z0-9-]{1,64}"))throw new IllegalArgumentException("잘못된 전송 식별자입니다.");
        // Same row lock as board writes: serializes room chat ID allocation/commit order.
        var room=rooms.lockForBoard(roomId).orElseThrow(()->new GlobalException(ErrorCode.NOT_FOUND,"회의실을 찾을 수 없습니다."));
        var member=member(roomId,userId);
        if(room.getEndedAt()!=null)throw new GlobalException(ErrorCode.CONFLICT,"종료된 회의실입니다.");
        var saved=messages.saveAndFlush(ChatMessage.builder().room(room).user(member.getUser())
            .messageType(MessageType.TEXT).content(req.message().trim()).pinned(false).build());
        return toDto(saved,req.clientMessageId());
    }
    @Transactional(readOnly=true)
    public ChatHistoryRes history(Long roomId,Long userId,String before,String after,int limit){
        member(roomId,userId);
        if(limit<1||limit>100||(before!=null&&after!=null))throw badCursor();
        Long beforeId=parseCursor(before),afterId=parseCursor(after);
        var query=PageRequest.of(0,limit+1);
        var found=afterId==null?messages.older(roomId,beforeId,query):messages.newer(roomId,afterId,query);
        boolean more=found.size()>limit;
        var page=new ArrayList<>(found.subList(0,Math.min(limit,found.size())));
        String next=more?String.valueOf(page.getLast().getId()):null;
        if(afterId==null)Collections.reverse(page);
        return new ChatHistoryRes(page.stream().map(m->toDto(m,null)).toList(),next,more);
    }
    @Transactional(readOnly=true)
    public ChatHistoryRes search(Long roomId,Long userId,String q,String before){
        member(roomId,userId);
        if(q==null||q.isBlank()||q.length()>200)throw new GlobalException(ErrorCode.BAD_REQUEST,"검색어는 1~200자여야 합니다.");
        var found=messages.search(roomId,q.trim(),parseCursor(before),PageRequest.of(0,51));
        boolean more=found.size()>50;var page=found.subList(0,Math.min(50,found.size()));
        return new ChatHistoryRes(page.stream().map(m->toDto(m,null)).toList(),more?String.valueOf(page.getLast().getId()):null,more);
    }
    private Long parseCursor(String raw){
        if(raw==null)return null;
        if(!raw.matches("[1-9][0-9]*"))throw badCursor();
        try{return Long.valueOf(raw);}catch(NumberFormatException e){throw badCursor();}
    }
    private GlobalException badCursor(){return new GlobalException(ErrorCode.BAD_REQUEST,"조회 범위 또는 커서가 올바르지 않습니다.");}
    private StoredChatMessage toDto(ChatMessage m,String clientId){
        return new StoredChatMessage(String.valueOf(m.getId()),m.getRoom().getId(),m.getUser().getUsername(),m.getContent(),
            m.getMessageType(),null,String.valueOf(m.getUser().getId()),m.getCreatedAt(),clientId);
    }
}
