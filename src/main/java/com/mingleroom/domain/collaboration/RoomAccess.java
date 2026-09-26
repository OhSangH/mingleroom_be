package com.mingleroom.domain.collaboration;
import com.mingleroom.common.enums.*;
import com.mingleroom.common.exception.GlobalException;
import com.mingleroom.domain.room.members.entity.RoomMember;
import com.mingleroom.domain.room.members.repository.RoomMemberRepository;
import com.mingleroom.domain.room.rooms.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
@Component @RequiredArgsConstructor
public class RoomAccess {
 private final RoomRepository rooms; private final RoomMemberRepository members;
 public RoomMember read(Long room,Long user){return members.findByIdRoomIdAndIdUserId(room,user).orElseThrow(()->error(ErrorCode.FORBIDDEN,"회의실 참가 권한이 없습니다."));}
 // Every mutation takes the same room lock, including first note creation and one-vote enforcement.
 public RoomMember write(Long room,Long user){var r=rooms.lockForBoard(room).orElseThrow(()->error(ErrorCode.NOT_FOUND,"회의실이 없습니다."));var m=read(room,user);if(r.getEndedAt()!=null)throw error(ErrorCode.CONFLICT,"종료된 회의실입니다.");return m;}
 public void host(RoomMember member){if(member.getRoleInRoom()!=RoomRole.HOST)throw error(ErrorCode.FORBIDDEN,"호스트만 사용할 수 있습니다.");}
 public void presenter(RoomMember member){if(member.getRoleInRoom()==RoomRole.MEMBER)throw error(ErrorCode.FORBIDDEN,"호스트 또는 발표자만 사용할 수 있습니다.");}
 public static GlobalException error(ErrorCode code,String text){return new GlobalException(code,text);}
}
