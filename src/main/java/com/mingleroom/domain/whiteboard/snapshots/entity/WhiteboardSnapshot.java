package com.mingleroom.domain.whiteboard.snapshots.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.mingleroom.common.global.BaseCreatedEntity;
import com.mingleroom.domain.users.entity.User;
import com.mingleroom.domain.whiteboard.pages.entity.WhiteboardPage;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@DynamicInsert
@Entity
@Table(name = "whiteboard_snapshots",
        uniqueConstraints = @UniqueConstraint(name = "uq_wbs_page_version", columnNames = {"page_id", "version"}),
        indexes = @Index(name = "idx_wbs_page_time", columnList = "page_id, created_at")
)
public class WhiteboardSnapshot extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "page_id", nullable = false)
    private WhiteboardPage page;

    @Column(nullable = false)
    private Integer version;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data_blob", nullable = false, columnDefinition = "jsonb")
    private JsonNode dataBlob;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
}