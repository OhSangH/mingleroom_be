package com.mingleroom.domain.room.rooms.repository;

import com.mingleroom.domain.room.rooms.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select r from Room r where r.id = :id")
    java.util.Optional<Room> lockForBoard(@org.springframework.data.repository.query.Param("id") Long id);

}
