package com.mingleroom.integration;
import com.mingleroom.common.enums.*;
import com.mingleroom.common.exception.GlobalException;
import com.mingleroom.domain.collaboration.*;
import com.mingleroom.domain.notes.entity.Note;
import com.mingleroom.domain.notes.repository.NoteRepository;
import com.mingleroom.domain.actionitems.entity.ActionItem;
import com.mingleroom.domain.actionitems.repository.ActionItemRepository;
import com.mingleroom.domain.bookmarks.entity.Bookmark;
import com.mingleroom.domain.bookmarks.repository.BookmarkRepository;
import com.mingleroom.domain.poll.polls.entity.Poll;
import com.mingleroom.domain.poll.polls.repository.PollRepository;
import com.mingleroom.domain.poll.option.entity.PollOption;
import com.mingleroom.domain.poll.option.repository.PollOptionRepository;
import com.mingleroom.domain.poll.votes.entity.PollVote;
import com.mingleroom.domain.poll.votes.repository.PollVoteRepository;
import com.mingleroom.domain.room.members.entity.*;
import com.mingleroom.domain.room.members.repository.RoomMemberRepository;
import com.mingleroom.domain.room.rooms.entity.Room;
import com.mingleroom.domain.room.rooms.repository.RoomRepository;
import com.mingleroom.domain.users.entity.User;
import org.junit.jupiter.api.*;
import java.util.*;
import static com.mingleroom.domain.collaboration.MeetingModels.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class MeetingServiceTest {
 RoomRepository rooms=mock(RoomRepository.class);RoomMemberRepository members=mock(RoomMemberRepository.class);
 NoteRepository notes=mock(NoteRepository.class);ActionItemRepository tasks=mock(ActionItemRepository.class);PollRepository polls=mock(PollRepository.class);PollOptionRepository options=mock(PollOptionRepository.class);PollVoteRepository votes=mock(PollVoteRepository.class);BookmarkRepository marks=mock(BookmarkRepository.class);
 RoomAccess access=new RoomAccess(rooms,members);MeetingService service=new MeetingService(access,notes,tasks,polls,options,votes,marks,members);
 Room room=Room.builder().id(1L).build();User user=User.builder().id(7L).username("테스트").build();
 void member(RoomRole role){when(members.findByIdRoomIdAndIdUserId(1L,7L)).thenReturn(Optional.of(RoomMember.builder().room(room).user(user).roleInRoom(role).build()));}
 @BeforeEach void setup(){when(rooms.lockForBoard(1L)).thenReturn(Optional.of(room));member(RoomRole.HOST);when(notes.save(any())).thenAnswer(i->i.getArgument(0));when(tasks.save(any())).thenAnswer(i->i.getArgument(0));}
 @Test void nonMemberCannotRead(){when(members.findByIdRoomIdAndIdUserId(1L,7L)).thenReturn(Optional.empty());assertThrows(GlobalException.class,()->service.get(1L,7L));verifyNoInteractions(notes,tasks,polls,marks);}
 @Test void memberCanCreateNote(){member(RoomRole.MEMBER);var v=service.saveNote(1L,7L,new NoteEdit("초안",0));assertEquals(1,v.version());assertEquals("테스트",v.author());}
 @Test void staleNoteCannotOverwrite(){var n=Note.builder().roomId(1L).content("최신").version(2).build();when(notes.findById(1L)).thenReturn(Optional.of(n));assertThrows(GlobalException.class,()->service.saveNote(1L,7L,new NoteEdit("덮어쓰기",1)));assertEquals("최신",n.getContent());verify(notes,never()).save(any());}
 @Test void noteVersionIncrements(){when(notes.findById(1L)).thenReturn(Optional.of(Note.builder().roomId(1L).content("이전").version(2).build()));assertEquals(3,service.saveNote(1L,7L,new NoteEdit("최신",2)).version());}
 TaskEdit taskEdit(int rev){return new TaskEdit("정리","설명",null,null,ActionStatus.TODO,rev);}
 @Test void memberCannotManageTasks(){member(RoomRole.MEMBER);assertThrows(GlobalException.class,()->service.saveTask(1L,7L,null,taskEdit(0)));verifyNoInteractions(tasks);}
 @Test void nonMemberCannotBeAssigned(){assertThrows(GlobalException.class,()->service.saveTask(1L,7L,null,new TaskEdit("제목",null,9L,null,ActionStatus.TODO,0)));verifyNoInteractions(tasks);}
 @Test void staleTaskCannotBeDeleted(){when(tasks.findById(2L)).thenReturn(Optional.of(ActionItem.builder().id(2L).room(room).revision(3).build()));assertThrows(GlobalException.class,()->service.deleteTask(1L,7L,2L,2));verify(tasks,never()).delete(any());}
 @Test void crossRoomTaskDenied(){when(tasks.findById(2L)).thenReturn(Optional.of(ActionItem.builder().room(Room.builder().id(9L).build()).revision(1).build()));assertThrows(GlobalException.class,()->service.saveTask(1L,7L,2L,taskEdit(1)));}
 @Test void presenterCanCreateTasks(){member(RoomRole.PRESENTER);assertEquals(1,service.saveTask(1L,7L,null,taskEdit(0)).revision());}
 @Test void duplicatePollChoicesRejected(){assertThrows(GlobalException.class,()->service.createPoll(1L,7L,new PollEdit("선택",List.of("A"," A "))));verifyNoInteractions(polls);}
 @Test void closedPollRejectsVote(){when(polls.findById(3L)).thenReturn(Optional.of(Poll.builder().id(3L).room(room).closedAt(java.time.OffsetDateTime.now()).build()));assertThrows(GlobalException.class,()->service.vote(1L,7L,3L,5L));verifyNoInteractions(votes);}
 @Test void foreignOptionRejected(){var p=Poll.builder().id(3L).room(room).build();when(polls.findById(3L)).thenReturn(Optional.of(p));when(options.findById(5L)).thenReturn(Optional.of(PollOption.builder().poll(Poll.builder().id(99L).build()).build()));assertThrows(GlobalException.class,()->service.vote(1L,7L,3L,5L));verifyNoInteractions(votes);}
 @Test void revoteUpdatesExistingVote(){member(RoomRole.MEMBER);var p=Poll.builder().id(3L).room(room).build();var o=PollOption.builder().id(5L).poll(p).label("A").build();var v=PollVote.builder().id(8L).poll(p).voter(user).build();when(polls.findById(3L)).thenReturn(Optional.of(p));when(options.findById(5L)).thenReturn(Optional.of(o));when(votes.findByPollIdAndVoterId(3L,7L)).thenReturn(Optional.of(v));when(votes.findByPollId(3L)).thenReturn(List.of(v));when(options.findByPollIdOrderBySortOrderAsc(3L)).thenReturn(List.of(o));assertEquals(5L,service.vote(1L,7L,3L,5L).myVote());verify(votes).saveAndFlush(v);assertSame(o,v.getOption());}
 @Test void memberCannotDeleteOthersBookmark(){member(RoomRole.MEMBER);when(marks.findById(2L)).thenReturn(Optional.of(Bookmark.builder().room(room).createdBy(User.builder().id(8L).build()).build()));assertThrows(GlobalException.class,()->service.deleteBookmark(1L,7L,2L));verify(marks,never()).delete(any());}
 @Test void endedRoomRejectsMutation(){when(rooms.lockForBoard(1L)).thenReturn(Optional.of(Room.builder().id(1L).endedAt(java.time.OffsetDateTime.now()).build()));assertThrows(GlobalException.class,()->service.saveNote(1L,7L,new NoteEdit("text",0)));verifyNoInteractions(notes);}
 @Test void bookmarkUsesServerRoomClock(){org.springframework.test.util.ReflectionTestUtils.setField(room,"createdAt",java.time.OffsetDateTime.now().minusSeconds(60));service.bookmark(1L,7L,new MarkEdit("결정"));verify(marks).save(argThat(b->b.getAtMs()>=59000&&b.getAtMs()<62000&&b.getLabel().equals("결정")));}
}
