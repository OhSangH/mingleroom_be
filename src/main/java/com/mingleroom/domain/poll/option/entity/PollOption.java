package com.mingleroom.domain.poll.option.entity;

import com.mingleroom.domain.poll.polls.entity.Poll;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@DynamicInsert
@Entity
@Table(name = "poll_options",
        uniqueConstraints = @UniqueConstraint(name = "uq_poll_options_sort", columnNames = {"poll_id", "sort_order"}),
        indexes = @Index(name = "idx_poll_options_poll", columnList = "poll_id")
)
public class PollOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "poll_id", nullable = false)
    private Poll poll;

    @Column(nullable = false, length = 150)
    private String label;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}