package com.mingleroom.domain.attachments.repository;

import com.mingleroom.domain.attachments.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
}
