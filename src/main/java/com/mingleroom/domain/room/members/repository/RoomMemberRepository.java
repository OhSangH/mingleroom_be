package com.mingleroom.domain.room.members.repository;

import com.mingleroom.domain.room.members.entity.RoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomMemberRepository extends JpaRepository<RoomMember, Long> {
}
