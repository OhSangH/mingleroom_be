package com.mingleroom.domain.chatmessages.controller;

import com.mingleroom.domain.chatmessages.dto.TestChatMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatWsController {

    @MessageMapping("/chat/room/{roomId}")
    @SendTo("/sub/chat/room/{roomId}")
    public TestChatMessage send(
            @DestinationVariable Long roomId,
            TestChatMessage message
    ){
        return new TestChatMessage  (roomId, message.sender(), message.message(), message.type(), message.eventType());
    }
}
