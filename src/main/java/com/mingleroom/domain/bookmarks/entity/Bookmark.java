package com.mingleroom.domain.bookmarks.entity;

import com.mingleroom.common.global.BaseCreatedEntity;
import com.mingleroom.domain.room.rooms.entity.Room;
import com.mingleroom.domain.users.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@DynamicInsert
@Entity
@Table(name = "bookmarks",
        indexes = @Index(name = "idx_bookmarks_room_time", columnList = "room_id, created_at")
)
public class Bookmark extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(nullable = false, length = 120)
    private String label;

    @Column(name = "at_ms", nullable = false)
    private Integer atMs;
}