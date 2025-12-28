package com.mingleroom.domain.room.invites.repository;

import com.mingleroom.domain.room.invites.entity.RoomInvite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomInviteRepository extends JpaRepository<RoomInvite, Long> {
}
