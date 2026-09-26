package com.mingleroom.domain.chatmessages.dto;
import java.util.List;
public record ChatHistoryRes(List<StoredChatMessage> items,String nextCursor,boolean hasMore) {}
