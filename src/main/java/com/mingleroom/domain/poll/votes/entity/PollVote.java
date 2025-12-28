package com.mingleroom.domain.poll.votes.entity;


import com.mingleroom.common.global.BaseCreatedEntity;
import com.mingleroom.domain.poll.option.entity.PollOption;
import com.mingleroom.domain.poll.polls.entity.Poll;
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
@Table(name = "poll_votes",
        indexes = @Index(name = "idx_poll_votes_option", columnList = "option_id")
)
public class PollVote extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "poll_id", nullable = false)
    private Poll poll;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "option_id", nullable = false)
    private PollOption option;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voter_id")
    private User voter;

    @Column(name = "voter_session_key", length = 64)
    private String voterSessionKey;

    // DB GENERATED ALWAYS AS (...) STORED
    @Column(name = "voter_key", insertable = false, updatable = false, columnDefinition = "text")
    private String voterKey;
}