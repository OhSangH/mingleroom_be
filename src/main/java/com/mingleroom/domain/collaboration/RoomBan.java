package com.mingleroom.domain.collaboration;
import com.mingleroom.domain.room.members.entity.RoomMemberId;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name="room_bans") @Getter @NoArgsConstructor @AllArgsConstructor
public class RoomBan { @EmbeddedId private RoomMemberId id; }
