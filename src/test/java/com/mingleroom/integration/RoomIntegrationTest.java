package com.mingleroom.integration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mingleroom.common.enums.*;
import com.mingleroom.common.exception.GlobalException;
import com.mingleroom.domain.room.rooms.entity.Room;
import com.mingleroom.domain.room.rooms.repository.RoomRepository;
import com.mingleroom.domain.room.rooms.service.RoomService;
import com.mingleroom.domain.room.members.repository.RoomMemberRepository;
import com.mingleroom.domain.room.events.repository.RoomEventRepository;
import com.mingleroom.domain.room.invites.repository.RoomInviteRepository;
import com.mingleroom.domain.users.entity.User;
import com.mingleroom.domain.users.repository.UserRepository;
import com.mingleroom.domain.workspace.workspaces.repository.WorkspaceRepository;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class RoomIntegrationTest {
    RoomRepository rooms=mock(RoomRepository.class);RoomMemberRepository members=mock(RoomMemberRepository.class);
    RoomEventRepository events=mock(RoomEventRepository.class);UserRepository users=mock(UserRepository.class);
    RoomService service=new RoomService(rooms,members,events,mock(RoomInviteRepository.class),users,mock(WorkspaceRepository.class),new ObjectMapper());
    void setup(Room room) {
        var user=mock(User.class);when(user.getId()).thenReturn(7L);
        when(users.findByEmail("test@example.test")).thenReturn(Optional.of(user));when(rooms.findById(1L)).thenReturn(Optional.of(room));
    }
    @Test void newMemberCannotJoinLockedRoom() {
        setup(Room.builder().id(1L).locked(true).visibility(RoomVisibility.PUBLIC).invitePolicy(InvitePolicy.LINK).build());
        assertThrows(GlobalException.class,()->service.joinRoom(1L,"test@example.test"));verify(members,never()).save(any());
    }
    @Test void privateRoomNeedsInvitation() {
        setup(Room.builder().id(1L).visibility(RoomVisibility.PRIVATE).invitePolicy(InvitePolicy.LINK).build());
        assertThrows(GlobalException.class,()->service.joinRoom(1L,"test@example.test"));verify(members,never()).save(any());
    }
    @Test void alreadyJoinedIsIdempotent() {
        setup(Room.builder().id(1L).locked(true).build());when(members.existsByIdRoomIdAndIdUserId(1L,7L)).thenReturn(true);
        service.joinRoom(1L,"test@example.test");verify(members,never()).save(any());verifyNoInteractions(events);
    }
    @Test void detailRequiresMembership() {
        assertThrows(GlobalException.class,()->service.getRoom(1L,7L));verifyNoInteractions(rooms);
    }
}
