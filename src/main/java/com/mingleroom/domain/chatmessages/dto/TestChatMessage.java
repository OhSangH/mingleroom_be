package com.mingleroom.domain.chatmessages.dto;

public record TestChatMessage(String roomId,
                              String sender,
                              String message,
                              String type) {
}
