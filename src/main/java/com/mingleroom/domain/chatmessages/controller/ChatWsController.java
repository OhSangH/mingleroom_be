package com.mingleroom.domain.chatmessages.controller;

import com.mingleroom.common.enums.MessageType;
import com.mingleroom.domain.chatmessages.dto.TestChatMessage;
import com.mingleroom.security.config.UserPrincipal;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;

@RestController
public class ChatWsController {
    @MessageMapping("/chat/room/{roomId}")
    @SendTo("/sub/chat/room/{roomId}")
    public TestChatMessage send(@DestinationVariable Long roomId, TestChatMessage message, Principal principal) {
        if (!(principal instanceof Authentication auth) || !(auth.getPrincipal() instanceof UserPrincipal user)) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }
        if (message.type() != MessageType.TEXT || message.message() == null
                || message.message().isBlank() || message.message().length() > 2000) {
            throw new IllegalArgumentException("텍스트 메시지는 1~2000자여야 합니다.");
        }
        // Sender and room are server-authoritative. Ignore client-supplied sender/eventType.
        return new TestChatMessage(roomId, user.getDisplayName(), message.message().trim(), MessageType.TEXT, null);
    }
}
