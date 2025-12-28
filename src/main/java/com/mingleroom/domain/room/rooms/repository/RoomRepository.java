package com.mingleroom.domain.room.rooms.repository;

import com.mingleroom.domain.room.rooms.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
}
