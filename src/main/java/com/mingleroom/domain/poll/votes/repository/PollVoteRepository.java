package com.mingleroom.domain.poll.votes.repository;

import com.mingleroom.domain.poll.votes.entity.PollVote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollVoteRepository extends JpaRepository<PollVote, Long> {
java.util.List<PollVote> findByPollId(Long pollId);
java.util.Optional<PollVote> findByPollIdAndVoterId(Long pollId,Long voterId);
}
