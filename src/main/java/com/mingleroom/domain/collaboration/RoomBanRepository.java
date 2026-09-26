package com.mingleroom.domain.collaboration;
import com.mingleroom.domain.room.members.entity.RoomMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RoomBanRepository extends JpaRepository<RoomBan,RoomMemberId>{}
