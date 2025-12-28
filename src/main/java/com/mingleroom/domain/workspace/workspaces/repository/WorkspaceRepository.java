package com.mingleroom.domain.workspace.workspaces.repository;

import com.mingleroom.domain.workspace.workspaces.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
}
