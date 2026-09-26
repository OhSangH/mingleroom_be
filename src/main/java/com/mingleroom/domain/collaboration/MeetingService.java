package com.mingleroom.domain.collaboration;
import com.mingleroom.common.enums.*;
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
import com.mingleroom.domain.room.members.repository.RoomMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.*;
import static com.mingleroom.domain.collaboration.RoomAccess.error;
import static com.mingleroom.domain.collaboration.MeetingModels.*;
@Service @RequiredArgsConstructor @Transactional
public class MeetingService {
 private final RoomAccess access; private final NoteRepository notes; private final ActionItemRepository tasks;
 private final PollRepository polls; private final PollOptionRepository options; private final PollVoteRepository votes;
 private final BookmarkRepository marks; private final RoomMemberRepository members;
 private NoteView noteView(Note n){return n==null?new NoteView("",0,null,null):new NoteView(n.getContent(),n.getVersion(),n.getUpdatedBy()==null?null:n.getUpdatedBy().getUsername(),n.getUpdatedAt());}
 private TaskView taskView(ActionItem t){return new TaskView(t.getId(),t.getTitle(),t.getDescription(),t.getAssignee()==null?null:t.getAssignee().getId(),t.getAssignee()==null?null:t.getAssignee().getUsername(),t.getDueDate(),t.getStatus(),t.getRevision());}
 private PollView pollView(Poll p,Long user){var vs=votes.findByPollId(p.getId());return new PollView(p.getId(),p.getQuestion(),p.getClosedAt()!=null,vs.stream().filter(v->v.getVoter()!=null&&Objects.equals(v.getVoter().getId(),user)).map(v->v.getOption().getId()).findFirst().orElse(null),options.findByPollIdOrderBySortOrderAsc(p.getId()).stream().map(o->new OptionView(o.getId(),o.getLabel(),vs.stream().filter(v->Objects.equals(v.getOption().getId(),o.getId())).count())).toList());}
 @Transactional(readOnly=true) public Summary get(Long room,Long user){access.read(room,user);return new Summary(noteView(notes.findById(room).orElse(null)),tasks.findByRoomIdOrderByIdDesc(room).stream().map(this::taskView).toList(),polls.findByRoomIdOrderByIdDesc(room).stream().map(p->pollView(p,user)).toList(),marks.findByRoomIdOrderByIdDesc(room).stream().map(b->new MarkView(b.getId(),b.getLabel(),b.getAtMs(),b.getCreatedBy()==null?null:b.getCreatedBy().getId(),b.getCreatedBy()==null?null:b.getCreatedBy().getUsername())).toList());}
 public NoteView saveNote(Long room,Long user,NoteEdit edit){var m=access.write(room,user);var n=notes.findById(room).orElse(null);if(edit.version()!=(n==null?0:n.getVersion()))throw error(ErrorCode.CONFLICT,"회의록이 변경됐습니다. 내 초안을 복사하고 최신 내용을 불러오세요.");if(n==null)n=Note.builder().room(m.getRoom()).roomId(room).content(edit.content()).updatedBy(m.getUser()).updatedAt(OffsetDateTime.now()).version(1).build();else n.revise(edit.content(),m.getUser());return noteView(notes.save(n));}
 public TaskView saveTask(Long room,Long user,Long id,TaskEdit edit){var m=access.write(room,user);access.presenter(m);var assignee=edit.assigneeId()==null?null:members.findByIdRoomIdAndIdUserId(room,edit.assigneeId()).orElseThrow(()->error(ErrorCode.BAD_REQUEST,"담당자는 회의실 참가자여야 합니다.")).getUser();ActionItem t;if(id==null){if(edit.revision()!=0)throw error(ErrorCode.CONFLICT,"새 할 일의 버전은 0입니다.");t=ActionItem.builder().room(m.getRoom()).title(edit.title().trim()).description(edit.description()).assignee(assignee).dueDate(edit.dueDate()).status(edit.status()).doneAt(edit.status()==ActionStatus.DONE?OffsetDateTime.now():null).revision(1).build();}else{t=task(room,id);if(t.getRevision()!=edit.revision())throw error(ErrorCode.CONFLICT,"할 일이 변경됐습니다. 다시 불러오세요.");t.revise(edit.title().trim(),edit.description(),assignee,edit.dueDate(),edit.status());}return taskView(tasks.save(t));}
 private ActionItem task(Long room,Long id){return tasks.findById(id).filter(t->Objects.equals(t.getRoom().getId(),room)).orElseThrow(()->error(ErrorCode.NOT_FOUND,"할 일이 없습니다."));}
 public void deleteTask(Long room,Long user,Long id,int revision){access.presenter(access.write(room,user));var t=task(room,id);if(t.getRevision()!=revision)throw error(ErrorCode.CONFLICT,"할 일이 변경됐습니다.");tasks.delete(t);}
 public PollView createPoll(Long room,Long user,PollEdit edit){var m=access.write(room,user);access.presenter(m);var labels=edit.options().stream().map(String::trim).toList();if(new HashSet<>(labels).size()!=labels.size())throw error(ErrorCode.BAD_REQUEST,"중복된 선택지는 사용할 수 없습니다.");var p=polls.save(Poll.builder().room(m.getRoom()).createdBy(m.getUser()).question(edit.question().trim()).anonymous(true).build());for(int i=0;i<labels.size();i++)options.save(PollOption.builder().poll(p).label(labels.get(i)).sortOrder(i).build());return pollView(p,user);}
 private Poll poll(Long room,Long id){return polls.findById(id).filter(p->Objects.equals(p.getRoom().getId(),room)).orElseThrow(()->error(ErrorCode.NOT_FOUND,"투표가 없습니다."));}
 public PollView vote(Long room,Long user,Long id,Long option){var m=access.write(room,user);var p=poll(room,id);if(p.getClosedAt()!=null)throw error(ErrorCode.CONFLICT,"마감된 투표입니다.");var o=options.findById(option).filter(v->Objects.equals(v.getPoll().getId(),id)).orElseThrow(()->error(ErrorCode.BAD_REQUEST,"이 투표의 선택지가 아닙니다."));var v=votes.findByPollIdAndVoterId(id,user).orElse(null);if(v==null)v=PollVote.builder().poll(p).option(o).voter(m.getUser()).build();else v.choose(o);votes.saveAndFlush(v);return pollView(p,user);}
 public PollView closePoll(Long room,Long user,Long id){access.presenter(access.write(room,user));var p=poll(room,id);p.close();return pollView(p,user);}
 public void bookmark(Long room,Long user,MarkEdit edit){var m=access.write(room,user);marks.save(Bookmark.builder().room(m.getRoom()).createdBy(m.getUser()).label(edit.label().trim()).atMs((int)Math.max(0,Math.min(Integer.MAX_VALUE,java.time.Duration.between(m.getRoom().getCreatedAt(),OffsetDateTime.now()).toMillis()))).build());}
 public void deleteBookmark(Long room,Long user,Long id){var m=access.write(room,user);var b=marks.findById(id).filter(v->Objects.equals(v.getRoom().getId(),room)).orElseThrow(()->error(ErrorCode.NOT_FOUND,"북마크가 없습니다."));if(m.getRoleInRoom()==RoomRole.MEMBER&&(b.getCreatedBy()==null||!Objects.equals(b.getCreatedBy().getId(),user)))throw error(ErrorCode.FORBIDDEN,"본인의 북마크만 삭제할 수 있습니다.");marks.delete(b);}
}
