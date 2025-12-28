package com.mingleroom.domain.reports.entity;


import com.mingleroom.common.global.BaseCreatedEntity;
import com.mingleroom.domain.enums.ReportStatus;
import com.mingleroom.domain.enums.ReportTargetType;
import com.mingleroom.domain.users.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@DynamicInsert
@Entity
@Table(name = "reports",
        indexes = {
                @Index(name = "idx_reports_status_time", columnList = "status, created_at"),
                @Index(name = "idx_reports_target", columnList = "target_type, target_id")
        }
)
public class Report extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private User reporter;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private ReportTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(nullable = false, length = 80)
    private String reason;

    @Column(columnDefinition = "text")
    private String detail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;
}