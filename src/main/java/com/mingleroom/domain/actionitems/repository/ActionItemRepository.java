package com.mingleroom.domain.actionitems.repository;

import com.mingleroom.domain.actionitems.entity.ActionItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionItemRepository extends JpaRepository<ActionItem, Long> {
}
