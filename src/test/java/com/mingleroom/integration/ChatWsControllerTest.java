package com.mingleroom.integration;
import com.mingleroom.domain.chatmessages.controller.ChatWsController;
import com.mingleroom.domain.chatmessages.dto.TestChatMessage;
import com.mingleroom.common.enums.MessageType;
import com.mingleroom.common.enums.RoomEventType;
import com.mingleroom.security.config.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class ChatWsControllerTest {
    @Test void ignoresSpoofedSenderRoomAndEvent() {
        var user=mock(UserPrincipal.class);when(user.getDisplayName()).thenReturn("실제 사용자");
        var auth=new UsernamePasswordAuthenticationToken(user,null,List.of());
        var result=new ChatWsController().send(1L,new TestChatMessage(999L,"가짜 발신자"," 안녕하세요 ",MessageType.TEXT,RoomEventType.JOIN),auth);
        assertEquals(1L,result.roomId());assertEquals("실제 사용자",result.sender());assertEquals("안녕하세요",result.message());assertNull(result.eventType());
    }
    @Test void rejectsEmptyOrOversizedText() {
        var auth=new UsernamePasswordAuthenticationToken(mock(UserPrincipal.class),null,List.of());
        for(String text:new String[]{" ","x".repeat(2001)})assertThrows(IllegalArgumentException.class,()->new ChatWsController().send(1L,new TestChatMessage(1L,"x",text,MessageType.TEXT,null),auth));
    }
}
