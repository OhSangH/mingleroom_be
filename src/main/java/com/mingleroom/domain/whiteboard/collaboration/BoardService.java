package com.mingleroom.domain.whiteboard.collaboration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mingleroom.common.enums.ErrorCode;
import com.mingleroom.common.enums.RoomRole;
import com.mingleroom.common.exception.GlobalException;
import com.mingleroom.domain.room.rooms.repository.RoomRepository;
import com.mingleroom.domain.room.rooms.entity.Room;
import com.mingleroom.domain.room.members.repository.RoomMemberRepository;
import com.mingleroom.domain.room.members.entity.RoomMember;
import com.mingleroom.domain.whiteboard.docs.repository.WhiteboardDocRepository;
import com.mingleroom.domain.whiteboard.docs.entity.WhiteboardDoc;
import com.mingleroom.domain.whiteboard.pages.repository.WhiteboardPageRepository;
import com.mingleroom.domain.whiteboard.pages.entity.WhiteboardPage;
import com.mingleroom.domain.whiteboard.snapshots.repository.WhiteboardSnapshotRepository;
import com.mingleroom.domain.whiteboard.snapshots.entity.WhiteboardSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.*;
import static com.mingleroom.domain.whiteboard.collaboration.BoardModels.*;
@Service
@RequiredArgsConstructor
@Transactional
public class BoardService {
    private final RoomRepository rooms;
    private final RoomMemberRepository members;
    private final WhiteboardDocRepository docs;
    private final WhiteboardPageRepository pages;
    private final WhiteboardSnapshotRepository snapshots;
    private final ObjectMapper mapper;
    private final SimpMessagingTemplate broker;
    private static final String SCHEMA="mingleroom-sticky-v1";
    private RoomMember member(Long roomId, Long userId) {
        return members.findByIdRoomIdAndIdUserId(roomId,userId)
            .orElseThrow(()->new GlobalException(ErrorCode.FORBIDDEN,"방 참가자만 보드를 사용할 수 있습니다."));
    }
    private void active(Room room) {
        if(room.getEndedAt()!=null) throw new GlobalException(ErrorCode.CONFLICT,"종료된 회의실입니다.");
    }
    private Optional<WhiteboardPage> page(Long roomId) {
        return docs.findFirstByRoomIdOrderBySortOrderAscIdAsc(roomId)
            .flatMap(d->pages.findFirstByDocIdOrderByPageNoAsc(d.getId()));
    }
    private State read(WhiteboardPage page) {
        return snapshots.findFirstByPageIdOrderByVersionDesc(page.getId()).map(s->{
            if(!SCHEMA.equals(s.getDataBlob().path("schema").asText()))
                throw new GlobalException(ErrorCode.CONFLICT,"다른 형식으로 저장된 보드입니다. 기존 데이터를 먼저 확인하세요.");
            State value=mapper.convertValue(s.getDataBlob(),State.class);
            return new State(SCHEMA,s.getVersion(),value.notes());
        }).orElse(new State(SCHEMA,0,List.of()));
    }
    @Transactional(readOnly=true)
    public State get(Long roomId,Long userId) {
        var m=member(roomId,userId);active(m.getRoom());
        return page(roomId).map(this::read).orElse(new State(SCHEMA,0,List.of()));
    }
    public State edit(Long roomId,Long userId,String id,Edit edit,Integer deleteRevision) {
        if(id==null||!id.matches("[a-zA-Z0-9-]{1,64}"))throw new GlobalException(ErrorCode.BAD_REQUEST,"잘못된 노트 번호입니다.");
        var room=rooms.lockForBoard(roomId).orElseThrow(()->new GlobalException(ErrorCode.NOT_FOUND,"방을 찾을 수 없습니다."));
        var m=member(roomId,userId);active(room);
        var existingPage=page(roomId);
        State state=existingPage.map(this::read).orElse(new State(SCHEMA,0,List.of()));
        var notes=new ArrayList<>(state.notes());
        var old=notes.stream().filter(n->n.id().equals(id)).findFirst().orElse(null);
        if(old!=null && m.getRoleInRoom()==RoomRole.MEMBER && !Objects.equals(old.authorId(),userId))
            throw new GlobalException(ErrorCode.FORBIDDEN,"참가자는 본인 노트만 편집할 수 있습니다.");
        int revision=edit==null ? (deleteRevision==null?-1:deleteRevision) : edit.revision();
        if(revision!=(old==null?0:old.revision()))throw new GlobalException(ErrorCode.CONFLICT,"다른 참가자가 수정했습니다. 최신 노트를 확인해 주세요.");
        if(edit==null && old==null)throw new GlobalException(ErrorCode.NOT_FOUND,"이미 삭제된 노트입니다.");
        if(edit!=null && (!Double.isFinite(edit.x())||!Double.isFinite(edit.y())))throw new GlobalException(ErrorCode.BAD_REQUEST,"잘못된 좌표입니다.");
        if(old==null && notes.size()>=300)throw new GlobalException(ErrorCode.CONFLICT,"한 보드에는 최대 300개의 노트를 만들 수 있습니다.");
        if(old!=null)notes.remove(old);
        int version=state.version()+1;
        if(edit!=null)notes.add(new Sticky(id,edit.text().trim(),edit.color(),edit.x(),edit.y(),
            old==null?userId:old.authorId(),old==null?m.getUser().getUsername():old.author(),version));
        var p=existingPage.orElseGet(()->{
            var d=docs.findFirstByRoomIdOrderBySortOrderAscIdAsc(roomId).orElseGet(()->docs.save(WhiteboardDoc.builder().room(room).title("아이디어 보드").sortOrder(0).build()));
            return pages.save(WhiteboardPage.builder().doc(d).pageNo(1).title("아이디어").build());
        });
        var next=new State(SCHEMA,version,List.copyOf(notes));
        snapshots.save(WhiteboardSnapshot.builder().page(p).version(version).dataBlob(mapper.valueToTree(next)).createdBy(m.getUser()).build());
        // Broadcast only committed state. REST polling recovers missed messages.
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){
            @Override public void afterCommit(){broker.convertAndSend("/sub/board/room/"+roomId,next);}
        });
        return next;
    }
}
