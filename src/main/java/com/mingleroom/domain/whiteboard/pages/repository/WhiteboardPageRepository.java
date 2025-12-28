package com.mingleroom.domain.whiteboard.pages.repository;

import com.mingleroom.domain.whiteboard.pages.entity.WhiteboardPage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WhiteboardPageRepository extends JpaRepository<WhiteboardPage, Long> {
}
