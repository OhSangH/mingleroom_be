package com.mingleroom.integration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mingleroom.domain.collaboration.*;
import com.mingleroom.domain.room.members.repository.RoomMemberRepository;
import com.mingleroom.domain.room.members.entity.RoomMember;
import com.mingleroom.domain.room.rooms.repository.RoomRepository;
import com.mingleroom.domain.room.rooms.entity.Room;
import com.mingleroom.security.config.UserPrincipal;
import com.mingleroom.common.exception.GlobalException;
import org.junit.jupiter.api.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class VoiceControllerTest {
 RoomMemberRepository members=mock(RoomMemberRepository.class);RoomRepository rooms=mock(RoomRepository.class);SimpMessagingTemplate broker=mock(SimpMessagingTemplate.class);VoiceController controller=new VoiceController(new RoomAccess(rooms,members),broker);UserPrincipal user=mock(UserPrincipal.class);UsernamePasswordAuthenticationToken auth=new UsernamePasswordAuthenticationToken(user,null,List.of());
 @BeforeEach void setup(){when(user.getId()).thenReturn(7L);var m=RoomMember.builder().room(Room.builder().id(1L).build()).build();when(members.findByIdRoomIdAndIdUserId(1L,7L)).thenReturn(Optional.of(m));when(members.findByIdRoomIdAndIdUserId(1L,8L)).thenReturn(Optional.of(m));}
 @Test void offerTargetsOnlyAuthorizedRecipient(){var data=new ObjectMapper().createObjectNode().put("sdp","test");controller.relay(1L,new VoiceController.Signal("OFFER",8L,data),auth);verify(broker).convertAndSend(eq("/sub/signal/room/1/user/8"),eq(new VoiceController.Relayed("OFFER",7L,8L,data)));}
 @Test void cannotSendOfferToOutsider(){assertThrows(GlobalException.class,()->controller.relay(1L,new VoiceController.Signal("OFFER",9L,null),auth));verifyNoInteractions(broker);}
 @Test void cannotBroadcastSdp(){assertThrows(GlobalException.class,()->controller.relay(1L,new VoiceController.Signal("JOIN",null,new ObjectMapper().createObjectNode()),auth));assertThrows(GlobalException.class,()->controller.relay(1L,new VoiceController.Signal("OFFER",null,null),auth));verifyNoInteractions(broker);}
 @Test void payloadSizeIsBounded(){assertThrows(GlobalException.class,()->controller.relay(1L,new VoiceController.Signal("OFFER",8L,new ObjectMapper().getNodeFactory().textNode("x".repeat(21000))),auth));verifyNoInteractions(broker);}
}
