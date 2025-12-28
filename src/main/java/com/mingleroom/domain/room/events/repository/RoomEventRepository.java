package com.mingleroom.domain.room.events.repository;

import com.mingleroom.domain.room.events.entity.RoomEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomEventRepository extends JpaRepository<RoomEvent, Long> {
}
