package com.mingleroom.domain.whiteboard.docs.entity;

import com.mingleroom.common.global.BaseTimeEntity;
import com.mingleroom.domain.room.rooms.entity.Room;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@DynamicInsert
@Entity
@Table(name = "whiteboard_docs",
        indexes = @Index(name = "idx_wbd_room_sort", columnList = "room_id, sort_order")
)
public class WhiteboardDoc extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}