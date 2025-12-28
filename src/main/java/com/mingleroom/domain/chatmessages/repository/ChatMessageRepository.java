package com.mingleroom.domain.chatmessages.repository;

import com.mingleroom.domain.chatmessages.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
}
