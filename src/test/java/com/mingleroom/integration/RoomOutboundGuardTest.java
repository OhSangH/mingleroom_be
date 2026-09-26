package com.mingleroom.integration;
import com.mingleroom.domain.room.members.repository.RoomMemberRepository;
import com.mingleroom.domain.room.rooms.repository.RoomRepository;
import com.mingleroom.domain.room.rooms.entity.Room;
import com.mingleroom.security.config.UserPrincipal;
import com.mingleroom.security.ws.RoomOutboundGuard;
import org.junit.jupiter.api.*;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.messaging.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class RoomOutboundGuardTest {
 RoomMemberRepository members=mock(RoomMemberRepository.class);RoomRepository rooms=mock(RoomRepository.class);RoomOutboundGuard guard=new RoomOutboundGuard(members,rooms);
 void connect(long expiry){var u=mock(UserPrincipal.class);when(u.getId()).thenReturn(7L);var auth=new UsernamePasswordAuthenticationToken(u,null,List.of());var c=StompHeaderAccessor.create(StompCommand.CONNECT);c.setSessionId("s1");c.setUser(auth);c.setSessionAttributes(new HashMap<>(Map.of("mingleroom.jwtExpiresAt",expiry)));var connected=StompHeaderAccessor.create(StompCommand.CONNECTED);connected.setSessionId("s1");connected.setHeader("simpConnectMessage",MessageBuilder.createMessage(new byte[0],c.getMessageHeaders()));guard.connected(new SessionConnectedEvent(this,MessageBuilder.createMessage(new byte[0],connected.getMessageHeaders()),auth));}
 Message<byte[]> outgoing(){var h=SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);h.setDestination("/sub/chat/room/1");h.setSessionId("s1");return MessageBuilder.createMessage(new byte[0],h.getMessageHeaders());}
 @BeforeEach void setup(){connect(System.currentTimeMillis()+60000);when(members.existsByIdRoomIdAndIdUserId(1L,7L)).thenReturn(true);when(rooms.findById(1L)).thenReturn(Optional.of(Room.builder().id(1L).build()));}
 @Test void subscribedMemberReceivesSimpMessage(){var m=outgoing();assertSame(m,guard.preSend(m,null));}
 @Test void removedMemberStopsReceiving(){when(members.existsByIdRoomIdAndIdUserId(1L,7L)).thenReturn(false);assertNull(guard.preSend(outgoing(),null));}
 @Test void expiredSubscriberStopsReceiving(){connect(0);assertNull(guard.preSend(outgoing(),null));}
 @Test void endedRoomStopsDelivering(){when(rooms.findById(1L)).thenReturn(Optional.of(Room.builder().id(1L).endedAt(java.time.OffsetDateTime.now()).build()));assertNull(guard.preSend(outgoing(),null));}
}
