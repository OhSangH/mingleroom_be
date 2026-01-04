package com.mingleroom.domain.attachments.entity;

import com.mingleroom.common.global.BaseCreatedEntity;
import com.mingleroom.common.enums.StorageProvider;
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
@Table(name = "attachments",
        indexes = {
                @Index(name = "idx_attachments_uploader", columnList = "uploader_id"),
                @Index(name = "idx_attachments_created", columnList = "created_at")
        }
)
public class Attachment extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id")
    private User uploader;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_provider", nullable = false, columnDefinition = "storage_provider_t")
    private StorageProvider storageProvider;

    @Column(name = "storage_key", nullable = false, length = 512)
    private String storageKey;

    @Column(name = "public_url", length = 1024)
    private String publicUrl;
}