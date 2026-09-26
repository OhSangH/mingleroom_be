package com.mingleroom.integration;
import com.mingleroom.domain.chatmessages.controller.ChatWsController;
import com.mingleroom.domain.chatmessages.dto.ChatSendReq;
import com.mingleroom.domain.chatmessages.service.ChatService;
import com.mingleroom.common.enums.MessageType;
import com.mingleroom.security.config.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.access.AccessDeniedException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class ChatWsControllerTest {
 @Test void usesPathRoomAndAuthenticatedUser(){
  var service=mock(ChatService.class);var user=mock(UserPrincipal.class);when(user.getId()).thenReturn(7L);
  var req=new ChatSendReq(999L,"위조","hello",MessageType.TEXT,"client-1");
  new ChatWsController(service).send(1L,req,new UsernamePasswordAuthenticationToken(user,null,List.of()));
  verify(service).send(1L,7L,req);
 }
 @Test void failedSaveCannotProduceBroadcastResponse(){
  var service=mock(ChatService.class);var user=mock(UserPrincipal.class);when(user.getId()).thenReturn(7L);
  var req=new ChatSendReq(1L,"x","hello",MessageType.TEXT,null);
  when(service.send(1L,7L,req)).thenThrow(new IllegalStateException("database unavailable"));
  assertThrows(IllegalStateException.class,()->new ChatWsController(service).send(1L,req,new UsernamePasswordAuthenticationToken(user,null,List.of())));
 }
 @Test void rejectsUnauthenticatedPrincipal(){var service=mock(ChatService.class);assertThrows(AccessDeniedException.class,()->new ChatWsController(service).send(1L,null,null));verifyNoInteractions(service);}
}
