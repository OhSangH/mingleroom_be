package com.mingleroom.domain.whiteboard.updates.entity;

import com.mingleroom.common.global.BaseCreatedEntity;
import com.mingleroom.domain.users.entity.User;
import com.mingleroom.domain.whiteboard.pages.entity.WhiteboardPage;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;



@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@DynamicInsert
@Entity
@Table(name = "whiteboard_updates",
        uniqueConstraints = @UniqueConstraint(name = "uq_wbu_page_seq", columnNames = {"page_id", "seq"}),
        indexes = @Index(name = "idx_wbu_page_time", columnList = "page_id, created_at")
)
public class WhiteboardUpdate extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "page_id", nullable = false)
    private WhiteboardPage page;

    @Column(nullable = false)
    private Long seq;

    @Column(name = "yjs_update", nullable = false)
    private byte[] yjsUpdate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
}