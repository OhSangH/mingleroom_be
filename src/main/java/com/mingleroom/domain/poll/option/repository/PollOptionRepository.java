package com.mingleroom.domain.poll.option.repository;

import com.mingleroom.domain.poll.option.entity.PollOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollOptionRepository extends JpaRepository<PollOption, Long> {
}
