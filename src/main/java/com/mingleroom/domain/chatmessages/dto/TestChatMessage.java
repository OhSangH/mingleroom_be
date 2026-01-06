package com.mingleroom.domain.chatmessages.dto;

import com.mingleroom.common.enums.MessageType;
import com.mingleroom.common.enums.RoomEventType;

public record TestChatMessage(Long roomId,
                              String sender,
                              String message,
                              MessageType type,
                              RoomEventType eventType
) {
}
