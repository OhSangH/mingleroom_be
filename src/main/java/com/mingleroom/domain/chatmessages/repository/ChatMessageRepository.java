package com.mingleroom.domain.chatmessages.repository;
import com.mingleroom.domain.chatmessages.entity.ChatMessage;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import java.util.List;
public interface ChatMessageRepository extends JpaRepository<ChatMessage,Long> {
    @Query("select m from ChatMessage m join fetch m.user where m.room.id=:roomId and m.deletedAt is null and (:before is null or m.id<:before) order by m.id desc")
    List<ChatMessage> older(@Param("roomId") Long roomId,@Param("before") Long before,Pageable pageable);
    @Query("select m from ChatMessage m join fetch m.user where m.room.id=:roomId and m.deletedAt is null and m.id>:after order by m.id asc")
    List<ChatMessage> newer(@Param("roomId") Long roomId,@Param("after") Long after,Pageable pageable);
}
