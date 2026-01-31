package com.mingleroom.domain.workspace.workspaces.repository;

import com.mingleroom.domain.workspace.workspaces.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
    List<Workspace> findAllByOwnerId(Long ownerId);


}
