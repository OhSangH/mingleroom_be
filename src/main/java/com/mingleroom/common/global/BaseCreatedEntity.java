package com.mingleroom.common.global;


import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.time.Instant;

@Getter
@MappedSuperclass
public abstract class BaseCreatedEntity {

    @Column(name = "created_at", nullable = false, updatable = false)
    protected Instant createdAt;
}