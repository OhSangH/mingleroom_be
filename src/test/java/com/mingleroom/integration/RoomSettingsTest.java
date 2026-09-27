package com.mingleroom.integration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mingleroom.common.enums.*;
import com.mingleroom.common.exception.GlobalException;
import com.mingleroom.domain.collaboration.RoomBanRepository;
import com.mingleroom.domain.room.rooms.dto.*;
import com.mingleroom.domain.room.rooms.entity.Room;
import com.mingleroom.domain.room.rooms.repository.RoomRepository;
import com.mingleroom.domain.room.rooms.service.RoomService;
import com.mingleroom.domain.room.members.entity.RoomMember;
import com.mingleroom.domain.room.members.repository.RoomMemberRepository;
import com.mingleroom.domain.room.events.repository.RoomEventRepository;
import com.mingleroom.domain.room.invites.repository.RoomInviteRepository;
import com.mingleroom.domain.users.entity.User;
import com.mingleroom.domain.users.repository.UserRepository;
import com.mingleroom.domain.workspace.workspaces.repository.WorkspaceRepository;
import com.mingleroom.domain.workspace.members.repository.WorkspaceMemberRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class RoomSettingsTest {
 RoomRepository rooms=mock(RoomRepository.class);RoomMemberRepository members=mock(RoomMemberRepository.class);RoomEventRepository events=mock(RoomEventRepository.class);RoomInviteRepository invites=mock(RoomInviteRepository.class);UserRepository users=mock(UserRepository.class);WorkspaceMemberRepository workspaceMembers=mock(WorkspaceMemberRepository.class);
 RoomService service=new RoomService(rooms,mock(RoomBanRepository.class),members,events,invites,users,mock(WorkspaceRepository.class),workspaceMembers,new ObjectMapper());
 User user=User.builder().id(7L).username("호스트").build();Room room=Room.builder().id(1L).host(user).title("회의").visibility(RoomVisibility.PUBLIC).invitePolicy(InvitePolicy.LINK).build();
 @BeforeEach void setup(){when(rooms.lockForBoard(1L)).thenReturn(Optional.of(room));role(RoomRole.HOST);when(users.findByEmail("host@example.test")).thenReturn(Optional.of(user));when(rooms.save(any())).thenAnswer(i->{Room r=i.getArgument(0);ReflectionTestUtils.setField(r,"id",1L);return r;});}
 void role(RoomRole role){when(members.findByIdRoomIdAndIdUserId(1L,7L)).thenReturn(Optional.of(RoomMember.builder().room(room).user(user).roleInRoom(role).build()));}
 RoomUpdateReq edit(){return new RoomUpdateReq(" 새 이름 ",RoomVisibility.PRIVATE,"회의",RoomVisibility.PUBLIC);}
 @ParameterizedTest @EnumSource(value=RoomVisibility.class,names={"PUBLIC","PRIVATE"}) void createsSelectedVisibilityWithHostMembership(RoomVisibility visibility){var result=service.createRoom("host@example.test",new RoomCreateReq(" 새 회의 ",visibility,InvitePolicy.LINK,null));assertEquals(visibility,result.visibility());assertEquals("새 회의",result.title());verify(members).save(argThat(m->m.getRoleInRoom()==RoomRole.HOST&&m.getId().getRoomId()==1L));}
 @Test void requiresWorkspaceForTeamRoom(){assertThrows(GlobalException.class,()->service.createRoom("host@example.test",new RoomCreateReq("팀",RoomVisibility.TEAM,InvitePolicy.LINK,null)));verify(rooms,never()).save(any());}
 @Test void rejectsUnimplementedPasswordPolicy(){assertThrows(GlobalException.class,()->service.createRoom("host@example.test",new RoomCreateReq("방",RoomVisibility.PUBLIC,InvitePolicy.PASSWORD,null)));verify(rooms,never()).save(any());}
 @Test void hostCanRenameAndMakePrivateWithoutRemovingMembersOrInvites(){var result=service.updateRoom(1L,7L,edit());assertEquals("새 이름",result.title());assertEquals(RoomVisibility.PRIVATE,result.visibility());verify(members,never()).delete(any());verifyNoInteractions(invites);verify(events).save(argThat(e->e.getPayload().path("action").asText().equals("ROOM_SETTINGS")));}
 @ParameterizedTest @EnumSource(value=RoomRole.class,names={"MEMBER","PRESENTER"}) void deniesNonHost(RoomRole role){role(role);assertThrows(GlobalException.class,()->service.updateRoom(1L,7L,edit()));assertEquals("회의",room.getTitle());verifyNoInteractions(events);}
 @Test void deniesOutsider(){when(members.findByIdRoomIdAndIdUserId(1L,7L)).thenReturn(Optional.empty());assertThrows(GlobalException.class,()->service.updateRoom(1L,7L,edit()));}
 @Test void rejectsStaleSettings(){room.updateDetails("다른 화면 수정",RoomVisibility.PRIVATE);assertThrows(GlobalException.class,()->service.updateRoom(1L,7L,edit()));assertEquals("다른 화면 수정",room.getTitle());verifyNoInteractions(events);}
 @Test void endedRoomCannotChange(){room.end();assertThrows(GlobalException.class,()->service.updateRoom(1L,7L,edit()));verifyNoInteractions(events);}
 @Test void cannotTurnPersonalRoomIntoTeam(){assertThrows(GlobalException.class,()->service.updateRoom(1L,7L,new RoomUpdateReq("회의",RoomVisibility.TEAM,"회의",RoomVisibility.PUBLIC)));}
 @Test void visibilityChangeDoesNotUnlockRoom(){room.setLocked(true);service.updateRoom(1L,7L,edit());assertTrue(room.isLocked());assertEquals(InvitePolicy.LINK,room.getInvitePolicy());}
}
