package com.mingleroom.integration;

import com.mingleroom.domain.room.members.repository.RoomMemberRepository;
import com.mingleroom.domain.room.rooms.entity.Room;
import com.mingleroom.domain.room.rooms.repository.RoomRepository;
import com.mingleroom.security.CustomUserDetailsService;
import com.mingleroom.security.config.UserPrincipal;
import com.mingleroom.security.jwt.JwtProvider;
import com.mingleroom.security.ws.StompAuthenticationInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import java.util.HashMap;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StompAuthenticationInterceptorTest {
    private static final String TEST_KEY = "local-contract-test-only-key-not-a-production-secret-0123456789abcdef";
    JwtProvider jwt = new JwtProvider(TEST_KEY, 60000);
    CustomUserDetailsService users = mock(CustomUserDetailsService.class);
    RoomMemberRepository members = mock(RoomMemberRepository.class);
    RoomRepository rooms = mock(RoomRepository.class);
    UserPrincipal principal = mock(UserPrincipal.class);
    StompAuthenticationInterceptor interceptor = new StompAuthenticationInterceptor(jwt, users, members, rooms);
    @BeforeEach void setup() {
        when(users.loadUserByUsername("test@example.test")).thenReturn(principal);
        when(principal.isEnabled()).thenReturn(true);
        when(principal.isAccountNonLocked()).thenReturn(true);
        when(principal.getId()).thenReturn(7L);
    }
    Message<byte[]> message(StompHeaderAccessor h) { h.setLeaveMutable(true); return MessageBuilder.createMessage(new byte[0], h.getMessageHeaders()); }
    StompHeaderAccessor connect() {
        var h = StompHeaderAccessor.create(StompCommand.CONNECT);
        h.setNativeHeader("Authorization", "Bearer " + jwt.createAccessToken(7L,"test@example.test","USER"));
        h.setSessionAttributes(new HashMap<>());
        interceptor.preSend(message(h),null);
        return h;
    }
    StompHeaderAccessor roomMessage(StompCommand command, String destination) {
        var connection = connect(); var h = StompHeaderAccessor.create(command);
        h.setUser(connection.getUser()); h.setSessionAttributes(connection.getSessionAttributes()); h.setDestination(destination); return h;
    }
    @Test void requiresTokenOnConnect() {
        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message(StompHeaderAccessor.create(StompCommand.CONNECT)),null));
    }
    @Test void rejectsInvalidToken() {
        var h=StompHeaderAccessor.create(StompCommand.CONNECT); h.setNativeHeader("Authorization","Bearer invalid");
        assertThrows(AccessDeniedException.class,()->interceptor.preSend(message(h),null));
    }
    @Test void rejectsExpiredToken() {
        var h=StompHeaderAccessor.create(StompCommand.CONNECT);
        h.setNativeHeader("Authorization","Bearer "+new JwtProvider(TEST_KEY,-1000).createAccessToken(7L,"test@example.test","USER"));
        assertThrows(AccessDeniedException.class,()->interceptor.preSend(message(h),null));
    }
    @Test void rejectsBannedAccount() {
        when(principal.isAccountNonLocked()).thenReturn(false);
        assertThrows(AccessDeniedException.class,this::connect);
    }
    @Test void permitsAuthenticatedMemberSubscription() {
        when(members.existsByIdRoomIdAndIdUserId(1L,7L)).thenReturn(true);
        when(rooms.findById(1L)).thenReturn(Optional.of(Room.builder().id(1L).build()));
        var m=message(roomMessage(StompCommand.SUBSCRIBE,"/sub/chat/room/1"));
        assertSame(m,interceptor.preSend(m,null));
    }
    @Test void deniesNonMemberSend() {
        var m=message(roomMessage(StompCommand.SEND,"/pub/chat/room/2"));
        assertThrows(AccessDeniedException.class,()->interceptor.preSend(m,null));
    }
    @Test void rejectsWrongDestinationAndOverflows() {
        for(String destination:new String[]{"/sub/chat/room/1","/pub/chat/room/1/extra","/pub/chat/room/999999999999999999999999"}) {
            var m=message(roomMessage(StompCommand.SEND,destination));
            assertThrows(AccessDeniedException.class,()->interceptor.preSend(m,null));
        }
    }
    @Test void checksExpiryAgainOnSend() {
        var h=roomMessage(StompCommand.SEND,"/pub/chat/room/1");
        h.getSessionAttributes().put("mingleroom.jwtExpiresAt",0L);
        assertThrows(AccessDeniedException.class,()->interceptor.preSend(message(h),null));
    }
    @Test void rejectsClosedRoom() {
        when(members.existsByIdRoomIdAndIdUserId(1L,7L)).thenReturn(true);
        when(rooms.findById(1L)).thenReturn(Optional.of(Room.builder().id(1L).endedAt(java.time.OffsetDateTime.now()).build()));
        var m=message(roomMessage(StompCommand.SEND,"/pub/chat/room/1"));
        assertThrows(AccessDeniedException.class,()->interceptor.preSend(m,null));
    }
    @Test void permitsBoardAndCursorButNeverClientBoardBroadcast() {
        when(members.existsByIdRoomIdAndIdUserId(1L,7L)).thenReturn(true);
        when(rooms.findById(1L)).thenReturn(Optional.of(Room.builder().id(1L).build()));
        for(String dest:new String[]{"/sub/board/room/1","/sub/cursor/room/1"}) {
            var m=message(roomMessage(StompCommand.SUBSCRIBE,dest));assertSame(m,interceptor.preSend(m,null));
        }
        var cursor=message(roomMessage(StompCommand.SEND,"/pub/cursor/room/1"));assertSame(cursor,interceptor.preSend(cursor,null));
        assertThrows(AccessDeniedException.class,()->interceptor.preSend(message(roomMessage(StompCommand.SEND,"/pub/board/room/1")),null));
    }
}
