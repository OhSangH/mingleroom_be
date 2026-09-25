package com.mingleroom.integration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mingleroom.common.enums.RoomRole;
import com.mingleroom.common.exception.GlobalException;
import com.mingleroom.domain.room.rooms.entity.Room;
import com.mingleroom.domain.room.rooms.repository.RoomRepository;
import com.mingleroom.domain.room.members.entity.RoomMember;
import com.mingleroom.domain.room.members.repository.RoomMemberRepository;
import com.mingleroom.domain.users.entity.User;
import com.mingleroom.domain.whiteboard.collaboration.*;
import com.mingleroom.domain.whiteboard.docs.entity.WhiteboardDoc;
import com.mingleroom.domain.whiteboard.docs.repository.WhiteboardDocRepository;
import com.mingleroom.domain.whiteboard.pages.entity.WhiteboardPage;
import com.mingleroom.domain.whiteboard.pages.repository.WhiteboardPageRepository;
import com.mingleroom.domain.whiteboard.snapshots.entity.WhiteboardSnapshot;
import com.mingleroom.domain.whiteboard.snapshots.repository.WhiteboardSnapshotRepository;
import org.junit.jupiter.api.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.util.*;
import static com.mingleroom.domain.whiteboard.collaboration.BoardModels.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class BoardServiceTest {
 RoomRepository rooms=mock(RoomRepository.class);RoomMemberRepository members=mock(RoomMemberRepository.class);
 WhiteboardDocRepository docs=mock(WhiteboardDocRepository.class);WhiteboardPageRepository pages=mock(WhiteboardPageRepository.class);
 WhiteboardSnapshotRepository snapshots=mock(WhiteboardSnapshotRepository.class);SimpMessagingTemplate broker=mock(SimpMessagingTemplate.class);
 ObjectMapper mapper=new ObjectMapper();BoardService service=new BoardService(rooms,members,docs,pages,snapshots,mapper,broker);
 Room room=Room.builder().id(1L).build();User user=mock(User.class);
 WhiteboardPage page=WhiteboardPage.builder().id(3L).build();
 @BeforeEach void setup(){
  when(user.getUsername()).thenReturn("실제 작성자");when(user.getId()).thenReturn(7L);
  when(rooms.lockForBoard(1L)).thenReturn(Optional.of(room));role(RoomRole.MEMBER);
  when(docs.findFirstByRoomIdOrderBySortOrderAscIdAsc(1L)).thenReturn(Optional.of(WhiteboardDoc.builder().id(2L).build()));
  when(pages.findFirstByDocIdOrderByPageNoAsc(2L)).thenReturn(Optional.of(page));
  TransactionSynchronizationManager.initSynchronization();
 }
 @AfterEach void cleanup(){TransactionSynchronizationManager.clearSynchronization();}
 void role(RoomRole role){when(members.findByIdRoomIdAndIdUserId(1L,7L)).thenReturn(Optional.of(RoomMember.builder().room(room).user(user).roleInRoom(role).build()));}
 void state(Long author){var state=new State("mingleroom-sticky-v1",4,List.of(new Sticky("note-1","기존 내용","yellow",10,10,author,"원래 작성자",4)));when(snapshots.findFirstByPageIdOrderByVersionDesc(3L)).thenReturn(Optional.of(WhiteboardSnapshot.builder().version(4).dataBlob(mapper.valueToTree(state)).build()));}
 Edit edit(int revision){return new Edit(revision,"새 내용","green",30.0,40.0);}
 @Test void nonMemberCannotRead(){when(members.findByIdRoomIdAndIdUserId(1L,7L)).thenReturn(Optional.empty());assertThrows(GlobalException.class,()->service.get(1L,7L));verifyNoInteractions(docs);}
 @Test void memberCannotEditOthers(){state(8L);assertThrows(GlobalException.class,()->service.edit(1L,7L,"note-1",edit(4),null));verify(snapshots,never()).save(any());}
 @Test void memberCannotDeleteOthers(){state(8L);assertThrows(GlobalException.class,()->service.edit(1L,7L,"note-1",null,4));}
 @Test void staleRevisionCannotOverwrite(){state(7L);assertThrows(GlobalException.class,()->service.edit(1L,7L,"note-1",edit(3),null));verify(snapshots,never()).save(any());}
 @Test void hostCanEditAndPreservesAuthor(){state(8L);role(RoomRole.HOST);var result=service.edit(1L,7L,"note-1",edit(4),null);assertEquals(5,result.version());assertEquals(8L,result.notes().getFirst().authorId());verifyNoInteractions(broker);TransactionSynchronizationManager.getSynchronizations().forEach(s->s.afterCommit());verify(broker).convertAndSend("/sub/board/room/1",result);}
 @Test void presenterCanDelete(){state(8L);role(RoomRole.PRESENTER);assertTrue(service.edit(1L,7L,"note-1",null,4).notes().isEmpty());}
 @Test void newNoteUsesServerIdentity(){var result=service.edit(1L,7L,"new-note",edit(0),null);assertEquals(7L,result.notes().getFirst().authorId());assertEquals("실제 작성자",result.notes().getFirst().author());}
 @Test void deletedNoteCannotBeResurrectedByStaleEdit(){assertThrows(GlobalException.class,()->service.edit(1L,7L,"note-1",edit(4),null));}
 @Test void rejectsForeignSnapshotSchema(){when(snapshots.findFirstByPageIdOrderByVersionDesc(3L)).thenReturn(Optional.of(WhiteboardSnapshot.builder().version(2).dataBlob(mapper.createObjectNode().put("schema","other-tool")).build()));assertThrows(GlobalException.class,()->service.edit(1L,7L,"new",edit(0),null));verify(snapshots,never()).save(any());}
 @Test void rejectsClosedRoom(){when(rooms.lockForBoard(1L)).thenReturn(Optional.of(Room.builder().id(1L).endedAt(java.time.OffsetDateTime.now()).build()));assertThrows(GlobalException.class,()->service.edit(1L,7L,"new",edit(0),null));}
 @Test void validatesRestPayload(){try(var factory=jakarta.validation.Validation.buildDefaultValidatorFactory()){var validator=factory.getValidator();assertFalse(validator.validate(new Edit(-1," ","red",Double.NaN,10000.0)).isEmpty());assertTrue(validator.validate(edit(0)).isEmpty());}}
}
