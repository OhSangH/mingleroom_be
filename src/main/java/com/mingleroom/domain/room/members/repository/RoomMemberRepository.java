package com.mingleroom.domain.room.members.repository;

import com.mingleroom.domain.room.members.entity.RoomMember;
import com.mingleroom.domain.room.members.entity.RoomMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface RoomMemberRepository extends JpaRepository<RoomMember, RoomMemberId> {
    boolean existsByIdRoomIdAndIdUserId(Long idRoomId, Long idUserId);

    Optional<RoomMember> findByIdRoomIdAndIdUserId(Long idRoomId, Long idUserId);
}
