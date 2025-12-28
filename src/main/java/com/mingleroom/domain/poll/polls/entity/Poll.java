package com.mingleroom.domain.poll.polls.entity;
import com.mingleroom.common.global.BaseCreatedEntity;
import com.mingleroom.domain.room.rooms.entity.Room;
import com.mingleroom.domain.users.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@DynamicInsert
@Entity
@Table(name = "polls",
        indexes = @Index(name = "idx_polls_room_time", columnList = "room_id, created_at")
)
public class Poll extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(nullable = false, length = 255)
    private String question;

    @Column(name = "is_anonymous", nullable = false)
    private boolean anonymous;

    @Column(name = "closed_at")
    private Instant closedAt;
}