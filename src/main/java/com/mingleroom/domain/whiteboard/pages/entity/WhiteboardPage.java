package com.mingleroom.domain.whiteboard.pages.entity;

import com.mingleroom.common.global.BaseCreatedEntity;
import com.mingleroom.domain.whiteboard.docs.entity.WhiteboardDoc;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@DynamicInsert
@Entity
@Table(name = "whiteboard_pages",
        uniqueConstraints = @UniqueConstraint(name = "uq_wbp_doc_page_no", columnNames = {"doc_id", "page_no"}),
        indexes = @Index(name = "idx_wbp_doc", columnList = "doc_id")
)
public class WhiteboardPage extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doc_id", nullable = false)
    private WhiteboardDoc doc;

    @Column(name = "page_no", nullable = false)
    private Integer pageNo;

    @Column(length = 150)
    private String title;
}