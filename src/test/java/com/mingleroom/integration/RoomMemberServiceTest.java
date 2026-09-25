package com.mingleroom.integration;
import com.mingleroom.domain.room.members.repository.RoomMemberRepository;
import com.mingleroom.domain.room.members.service.RoomMemberService;
import com.mingleroom.common.exception.GlobalException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class RoomMemberServiceTest {
    @Test void nonMemberCannotReadMemberData() {
        var repo=mock(RoomMemberRepository.class);
        assertThrows(GlobalException.class,()->new RoomMemberService(repo).getRoomMember(1L,7L));
        verify(repo,never()).findByRoomId(anyLong());
    }
}
