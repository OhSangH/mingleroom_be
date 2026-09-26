package com.mingleroom.integration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mingleroom.common.enums.*;
import com.mingleroom.common.exception.GlobalException;
import com.mingleroom.domain.collaboration.*;
import com.mingleroom.domain.room.members.entity.*;
import com.mingleroom.domain.room.members.repository.RoomMemberRepository;
import com.mingleroom.domain.room.rooms.entity.Room;
import com.mingleroom.domain.room.rooms.repository.RoomRepository;
import com.mingleroom.domain.room.events.repository.RoomEventRepository;
import com.mingleroom.domain.room.invites.entity.RoomInvite;
import com.mingleroom.domain.room.invites.repository.RoomInviteRepository;
import com.mingleroom.domain.users.entity.User;
import com.mingleroom.domain.users.repository.UserRepository;
import org.junit.jupiter.api.*;
import java.util.*;
import java.time.OffsetDateTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class RoomControlsTest {
 RoomRepository rooms=mock(RoomRepository.class);RoomMemberRepository members=mock(RoomMemberRepository.class);RoomEventRepository events=mock(RoomEventRepository.class);RoomBanRepository bans=mock(RoomBanRepository.class);RoomInviteRepository invites=mock(RoomInviteRepository.class);UserRepository users=mock(UserRepository.class);
 RoomAccess access=new RoomAccess(rooms,members);RoomControls service=new RoomControls(access,members,events,bans,invites,rooms,users,new ObjectMapper());
 Room room=Room.builder().id(1L).build();User host=User.builder().id(7L).build(),other=User.builder().id(8L).build();RoomMember hm=RoomMember.builder().room(room).user(host).roleInRoom(RoomRole.HOST).build(),om=RoomMember.builder().room(room).user(other).roleInRoom(RoomRole.MEMBER).build();
 @BeforeEach void setup(){when(rooms.lockForBoard(1L)).thenReturn(Optional.of(room));when(members.findByIdRoomIdAndIdUserId(1L,7L)).thenReturn(Optional.of(hm));when(members.findByIdRoomIdAndIdUserId(1L,8L)).thenReturn(Optional.of(om));when(users.findById(8L)).thenReturn(Optional.of(other));when(members.save(any())).thenAnswer(i->i.getArgument(0));when(invites.save(any())).thenAnswer(i->i.getArgument(0));}
 @Test void memberCannotLock(){assertThrows(GlobalException.class,()->service.lock(1L,8L,true));assertFalse(room.isLocked());}
 @Test void hostLockIsAudited(){service.lock(1L,7L,true);assertTrue(room.isLocked());verify(events).save(any());}
 @Test void hostCannotBeDemotedOrGranted(){assertThrows(GlobalException.class,()->service.role(1L,7L,7L,RoomRole.MEMBER));assertThrows(GlobalException.class,()->service.role(1L,7L,8L,RoomRole.HOST));}
 @Test void hostCanAssignPresenter(){service.role(1L,7L,8L,RoomRole.PRESENTER);assertEquals(RoomRole.PRESENTER,om.getRoleInRoom());}
 @Test void kickAddsPersistentBan(){service.kick(1L,7L,8L);verify(bans).save(argThat(b->b.getId().equals(new RoomMemberId(1L,8L))));verify(members).delete(om);}
 @Test void bannedAccountCannotRedeem(){when(bans.existsById(new RoomMemberId(1L,8L))).thenReturn(true);assertThrows(GlobalException.class,()->service.redeem(1L,8L,"raw"));verifyNoInteractions(invites);}
 @Test void lockPreventsInviteRedemption(){room.setLocked(true);assertThrows(GlobalException.class,()->service.redeem(1L,8L,"raw"));verifyNoInteractions(invites);}
 RoomInvite invitation(OffsetDateTime expiry,int used,int max,boolean revoked){return RoomInvite.builder().room(room).inviteType(InviteType.LINK).expiresAt(expiry).usedCount(used).maxUses(max).revoked(revoked).build();}
 @Test void expiredExhaustedAndRevokedInvitesDenied(){for(var i:List.of(invitation(OffsetDateTime.now().minusMinutes(1),0,1,false),invitation(OffsetDateTime.now().plusHours(1),1,1,false),invitation(OffsetDateTime.now().plusHours(1),0,1,true))){when(invites.findByToken(RoomControls.hash("raw"))).thenReturn(Optional.of(i));assertThrows(GlobalException.class,()->service.redeem(1L,8L,"raw"));}verify(members,never()).save(any());}
 @Test void validInviteConsumesOnceAndAlwaysCreatesMember(){var i=invitation(OffsetDateTime.now().plusHours(1),0,1,false);when(invites.findByToken(RoomControls.hash("raw"))).thenReturn(Optional.of(i));service.redeem(1L,8L,"raw");assertEquals(1,i.getUsedCount());verify(members).save(argThat(m->m.getRoleInRoom()==RoomRole.MEMBER));when(members.existsByIdRoomIdAndIdUserId(1L,8L)).thenReturn(true);service.redeem(1L,8L,"raw");assertEquals(1,i.getUsedCount());}
 @Test void inviteStoresHashNotRawToken(){var out=service.invite(1L,7L,new RoomControls.InviteEdit(24,1));assertEquals(43,out.token().length());verify(invites).save(argThat(i->i.getToken().equals(RoomControls.hash(out.token()))&&!i.getToken().equals(out.token())));}
 @Test void handRaisingDoesNotChangeOtherMember(){service.hand(1L,8L,true);assertTrue(om.isHandRaised());assertFalse(hm.isHandRaised());}
 @Test void endLocksAndEndsRoom(){service.end(1L,7L);assertNotNull(room.getEndedAt());assertTrue(room.isLocked());}
}
