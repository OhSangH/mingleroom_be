package com.mingleroom.domain.whiteboard.updates.repository;

import com.mingleroom.domain.whiteboard.updates.entity.WhiteboardUpdate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WhiteboardUpdateRepository extends JpaRepository<WhiteboardUpdate, Long> {
}
