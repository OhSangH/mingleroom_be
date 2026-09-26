package com.mingleroom.integration;
import com.mingleroom.common.enums.*;
import com.mingleroom.common.exception.GlobalException;
import com.mingleroom.domain.chatmessages.dto.ChatSendReq;
import com.mingleroom.domain.chatmessages.entity.ChatMessage;
import com.mingleroom.domain.chatmessages.repository.ChatMessageRepository;
import com.mingleroom.domain.chatmessages.service.ChatService;
import com.mingleroom.domain.room.members.entity.RoomMember;
import com.mingleroom.domain.room.members.repository.RoomMemberRepository;
import com.mingleroom.domain.room.rooms.entity.Room;
import com.mingleroom.domain.room.rooms.repository.RoomRepository;
import com.mingleroom.domain.users.entity.User;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.OffsetDateTime;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class ChatServiceTest {
 ChatMessageRepository messages=mock(ChatMessageRepository.class);RoomMemberRepository members=mock(RoomMemberRepository.class);RoomRepository rooms=mock(RoomRepository.class);
 ChatService service=new ChatService(messages,members,rooms);Room room=Room.builder().id(1L).build();User user=mock(User.class);
 @BeforeEach void setup(){when(user.getId()).thenReturn(7L);when(user.getUsername()).thenReturn("서버 사용자");when(members.findByIdRoomIdAndIdUserId(1L,7L)).thenReturn(Optional.of(RoomMember.builder().room(room).user(user).build()));when(rooms.lockForBoard(1L)).thenReturn(Optional.of(room));}
 ChatMessage message(long id){var m=ChatMessage.builder().id(id).room(room).user(user).messageType(MessageType.TEXT).content("메시지 "+id).build();ReflectionTestUtils.setField(m,"createdAt",OffsetDateTime.parse("2026-09-26T12:00:00Z"));return m;}
 @Test void persistsServerIdentityAndAcknowledgesCommittedFields(){
  when(messages.saveAndFlush(any())).thenAnswer(i->{ChatMessage m=i.getArgument(0);ReflectionTestUtils.setField(m,"id",42L);ReflectionTestUtils.invokeMethod(m,"initializeCreatedAt");return m;});
  var dto=service.send(1L,7L,new ChatSendReq(99L,"가짜 발신자"," hello ",MessageType.TEXT,"request-1"));
  assertEquals("42",dto.id());assertEquals(1L,dto.roomId());assertEquals("서버 사용자",dto.sender());assertEquals("7",dto.senderId());assertEquals("hello",dto.message());assertNotNull(dto.createdAt());assertEquals("request-1",dto.clientMessageId());
 }
 @Test void validatesTextAndRequestId(){for(String text:new String[]{" ","x".repeat(2001)})assertThrows(IllegalArgumentException.class,()->service.send(1L,7L,new ChatSendReq(1L,"x",text,MessageType.TEXT,null)));assertThrows(IllegalArgumentException.class,()->service.send(1L,7L,new ChatSendReq(1L,"x","ok",MessageType.IMAGE,null)));assertThrows(IllegalArgumentException.class,()->service.send(1L,7L,new ChatSendReq(1L,"x","ok",MessageType.TEXT,"bad/id")));verifyNoInteractions(messages);}
 @Test void nonMemberCannotSendOrRead(){when(members.findByIdRoomIdAndIdUserId(1L,7L)).thenReturn(Optional.empty());assertThrows(GlobalException.class,()->service.history(1L,7L,null,null,50));assertThrows(GlobalException.class,()->service.send(1L,7L,new ChatSendReq(1L,"x","hi",MessageType.TEXT,null)));verifyNoInteractions(messages);}
 @Test void closedRoomCannotSend(){when(rooms.lockForBoard(1L)).thenReturn(Optional.of(Room.builder().id(1L).endedAt(OffsetDateTime.now()).build()));assertThrows(GlobalException.class,()->service.send(1L,7L,new ChatSendReq(1L,"x","hi",MessageType.TEXT,null)));verifyNoInteractions(messages);}
 @Test void historyReturnsAscendingItemsWithOlderCursor(){when(messages.older(eq(1L),isNull(),any())).thenReturn(List.of(message(9),message(8),message(7)));var p=service.history(1L,7L,null,null,2);assertEquals(List.of("8","9"),p.items().stream().map(x->x.id()).toList());assertEquals("8",p.nextCursor());assertTrue(p.hasMore());}
 @Test void reconnectPaginatesForward(){when(messages.newer(eq(1L),eq(7L),any())).thenReturn(List.of(message(8),message(9),message(10)));var p=service.history(1L,7L,null,"7",2);assertEquals(List.of("8","9"),p.items().stream().map(x->x.id()).toList());assertEquals("9",p.nextCursor());assertTrue(p.hasMore());}
 @Test void lastPageHasNoCursor(){when(messages.older(eq(1L),eq(8L),any())).thenReturn(List.of(message(7)));var p=service.history(1L,7L,"8",null,50);assertNull(p.nextCursor());assertFalse(p.hasMore());}
 @Test void emptyRoomReturnsEmptyHistory(){when(messages.older(eq(1L),isNull(),any())).thenReturn(List.of());assertTrue(service.history(1L,7L,null,null,50).items().isEmpty());}
 @Test void invalidPaginationFailsBeforeQuery(){for(String cursor:new String[]{"0","-1","xyz","999999999999999999999"})assertThrows(GlobalException.class,()->service.history(1L,7L,cursor,null,50));assertThrows(GlobalException.class,()->service.history(1L,7L,"2","3",50));for(int limit:new int[]{0,101})assertThrows(GlobalException.class,()->service.history(1L,7L,null,null,limit));verifyNoInteractions(messages);}
}
