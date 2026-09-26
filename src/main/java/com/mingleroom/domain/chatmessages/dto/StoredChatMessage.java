package com.mingleroom.domain.chatmessages.dto;
import com.mingleroom.common.enums.MessageType;
import com.mingleroom.common.enums.RoomEventType;
import java.time.OffsetDateTime;
public record StoredChatMessage(String id,Long roomId,String sender,String message,MessageType type,
    RoomEventType eventType,String senderId,OffsetDateTime createdAt,String clientMessageId) {}
