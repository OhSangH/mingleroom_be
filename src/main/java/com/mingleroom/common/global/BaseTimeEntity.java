package com.mingleroom.common.global;


import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@MappedSuperclass
public abstract class BaseTimeEntity extends BaseCreatedEntity{

    @Column(name = "updated_at", nullable = false)
    protected OffsetDateTime updatedAt;
}