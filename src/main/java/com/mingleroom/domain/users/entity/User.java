package com.mingleroom.domain.users.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mingleroom.common.global.BaseTimeEntity;
import com.mingleroom.common.enums.RoleGlobal;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Setter
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
    @JsonIgnore
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "role_global", nullable = false, columnDefinition = "role_global_t")
    private RoleGlobal roleGlobal;

    @Column(name = "is_banned", nullable = false)
    private boolean banned;

    @Column(name = "password_updated_at", nullable = false)
    private OffsetDateTime passwordUpdatedAt;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;
}
