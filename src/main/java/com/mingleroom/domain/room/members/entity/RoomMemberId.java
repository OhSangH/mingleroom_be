package com.mingleroom.domain.room.members.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class RoomMemberId implements Serializable {
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "user_id")
    private Long userId;
}