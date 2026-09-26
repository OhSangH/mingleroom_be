package com.mingleroom.domain.chatmessages.controller;
import com.mingleroom.domain.chatmessages.dto.ChatSendReq;
import com.mingleroom.domain.chatmessages.dto.StoredChatMessage;
import com.mingleroom.domain.chatmessages.service.ChatService;
import com.mingleroom.security.config.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import java.security.Principal;
@Controller
@RequiredArgsConstructor
public class ChatWsController {
    private final ChatService service;
    @MessageMapping("/chat/room/{roomId}") @SendTo("/sub/chat/room/{roomId}")
    public StoredChatMessage send(@DestinationVariable Long roomId,ChatSendReq message,Principal principal){
        if(!(principal instanceof Authentication auth)||!auth.isAuthenticated()||!(auth.getPrincipal() instanceof UserPrincipal user))
            throw new AccessDeniedException("로그인이 필요합니다.");
        // Transactional service commits before returning; a failed save is never broadcast.
        return service.send(roomId,user.getId(),message);
    }
}
