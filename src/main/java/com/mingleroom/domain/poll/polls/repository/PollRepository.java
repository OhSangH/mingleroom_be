package com.mingleroom.domain.poll.polls.repository;

import com.mingleroom.domain.poll.polls.entity.Poll;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollRepository extends JpaRepository<Poll,Long>{
}
