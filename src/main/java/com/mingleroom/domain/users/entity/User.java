package com.mingleroom.domain.users.entity;

import com.mingleroom.common.global.BaseTimeEntity;
import com.mingleroom.domain.enums.RoleGlobal;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@DynamicInsert
@Entity
@Table(name = "users")
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "citext", nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(name = "profile_img", length = 1024)
    private String profileImg;

    @Column(name = "password_hash", nullable = false, columnDefinition = "text")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_global", nullable = false, length = 20)
    private RoleGlobal roleGlobal;

    @Column(name = "is_banned", nullable = false)
    private boolean banned;

    @Column(name = "password_updated_at", nullable = false)
    private Instant passwordUpdatedAt;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;
}
