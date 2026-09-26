package com.mingleroom.domain.collaboration;
import com.fasterxml.jackson.databind.JsonNode;
import com.mingleroom.security.config.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import java.security.Principal;
import java.util.Set;
import com.mingleroom.common.enums.ErrorCode;
@Controller @RequiredArgsConstructor
public class VoiceController {
 private final RoomAccess access; private final SimpMessagingTemplate broker;
 public record Signal(String type,Long target,JsonNode data){}
 public record Relayed(String type,Long sender,Long target,JsonNode data){}
 @MessageMapping("/signal/room/{roomId}") @Transactional(readOnly=true)
 public void relay(@DestinationVariable Long roomId,Signal signal,Principal principal){
  var user=(UserPrincipal)((Authentication)principal).getPrincipal();var m=access.read(roomId,user.getId());
  if(m.getRoom().getEndedAt()!=null)throw RoomAccess.error(ErrorCode.FORBIDDEN,"종료된 회의실입니다.");
  if(signal==null||signal.type()==null||!Set.of("JOIN","LEAVE","HELLO","OFFER","ANSWER","ICE").contains(signal.type()))throw RoomAccess.error(ErrorCode.BAD_REQUEST,"지원하지 않는 시그널입니다.");
  if(signal.data()!=null&&signal.data().toString().length()>20000)throw RoomAccess.error(ErrorCode.BAD_REQUEST,"시그널 크기를 초과했습니다.");
  boolean broadcast=Set.of("JOIN","LEAVE").contains(signal.type());
  if(broadcast&&(signal.target()!=null||signal.data()!=null))throw RoomAccess.error(ErrorCode.BAD_REQUEST,"잘못된 참여 시그널입니다.");
  if(!broadcast){if(signal.target()==null||signal.target().equals(user.getId()))throw RoomAccess.error(ErrorCode.BAD_REQUEST,"수신자를 확인하세요.");access.read(roomId,signal.target());}
  broker.convertAndSend("/sub/signal/room/"+roomId+(broadcast?"":"/user/"+signal.target()),new Relayed(signal.type(),user.getId(),signal.target(),signal.data()));
 }
}
