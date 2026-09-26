package com.mingleroom.domain.chatmessages.dto;
import com.mingleroom.common.enums.MessageType;
// Existing sender/room fields may be sent by old clients, but are never trusted.
public record ChatSendReq(Long roomId,String sender,String message,MessageType type,String clientMessageId) {}
