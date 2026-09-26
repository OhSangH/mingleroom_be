package com.mingleroom.integration;
import com.mingleroom.domain.workspace.workspaces.repository.WorkspaceRepository;
import com.mingleroom.domain.workspace.workspaces.entity.Workspace;
import com.mingleroom.domain.workspace.workspaces.service.WorkspaceService;
import com.mingleroom.domain.workspace.members.service.WorkspaceMemberService;
import com.mingleroom.domain.workspace.members.repository.WorkspaceMemberRepository;
import com.mingleroom.domain.users.repository.UserRepository;
import com.mingleroom.domain.users.entity.User;
import com.mingleroom.security.config.UserPrincipal;
import com.mingleroom.common.exception.GlobalException;
import com.mingleroom.common.enums.WorkspaceRole;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class WorkspacePermissionTest {
 @Test void regularUserCannotTransferAnotherOwnersWorkspace(){var repos=mock(WorkspaceRepository.class);var members=mock(WorkspaceMemberService.class);var service=new WorkspaceService(repos,members,mock(UserRepository.class),mock(EntityManager.class));when(repos.findById(1L)).thenReturn(Optional.of(Workspace.builder().id(1L).owner(User.builder().id(7L).build()).build()));var user=mock(UserPrincipal.class);when(user.getId()).thenReturn(8L);when(user.getRole()).thenReturn("USER");assertThrows(GlobalException.class,()->service.transferWorkspaceOwnership(1L,9L,user));verifyNoInteractions(members);}
 @Test void evenAdminMustUseTransferInsteadOfCreatingSecondOwner(){var members=mock(WorkspaceMemberRepository.class);var service=new WorkspaceMemberService(members,mock(WorkspaceRepository.class),mock(UserRepository.class));var user=mock(UserPrincipal.class);when(user.getRole()).thenReturn("ADMIN");assertThrows(GlobalException.class,()->service.addWorkspaceMember(1L,"test@example.test",WorkspaceRole.OWNER,user));verifyNoInteractions(members);}
}
