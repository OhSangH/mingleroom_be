package com.mingleroom.domain.workspace.members.repository;

import com.mingleroom.domain.workspace.members.entity.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember,Long> {
}
