package com.mingleroom.domain.actionitems.entity;


import com.mingleroom.common.global.BaseTimeEntity;
import com.mingleroom.domain.enums.ActionStatus;
import com.mingleroom.domain.room.rooms.entity.Room;
import com.mingleroom.domain.users.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@DynamicInsert
@Entity
@Table(name = "action_items",
        indexes = {
                @Index(name = "idx_action_room_status", columnList = "room_id, status"),
                @Index(name = "idx_action_assignee", columnList = "assignee_id"),
                @Index(name = "idx_action_due", columnList = "due_date")
        }
)
public class ActionItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActionStatus status;

    @Column(name = "done_at")
    private Instant doneAt;
}