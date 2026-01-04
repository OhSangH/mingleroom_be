package com.mingleroom.domain.chatmessages.dto;

import com.mingleroom.common.enums.MessageType;

public record TestChatMessage(Long roomId,
                              String sender,
                              String message,
                              MessageType type) {
}
