package com.mingleroom.domain.whiteboard.snapshots.repository;

import com.mingleroom.domain.whiteboard.snapshots.entity.WhiteboardSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WhiteboardSnapshotRepository extends JpaRepository<WhiteboardSnapshot, Long> {
}
