package com.mingleroom.domain.whiteboard.docs.repository;

import com.mingleroom.domain.whiteboard.docs.entity.WhiteboardDoc;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WhiteboardDocRepository extends JpaRepository<WhiteboardDoc, Long> {
}
