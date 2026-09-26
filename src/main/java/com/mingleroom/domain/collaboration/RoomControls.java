package com.mingleroom.domain.collaboration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mingleroom.common.enums.*;
import com.mingleroom.domain.room.members.entity.*;
import com.mingleroom.domain.room.members.repository.RoomMemberRepository;
import com.mingleroom.domain.room.events.entity.RoomEvent;
import com.mingleroom.domain.room.events.repository.RoomEventRepository;
import com.mingleroom.domain.room.invites.entity.RoomInvite;
import com.mingleroom.domain.room.invites.repository.RoomInviteRepository;
import com.mingleroom.domain.room.rooms.repository.RoomRepository;
import com.mingleroom.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.*;
import java.security.*;
import java.nio.charset.StandardCharsets;
import static com.mingleroom.domain.collaboration.RoomAccess.error;
@Service @RequiredArgsConstructor @Transactional
public class RoomControls {
 private final RoomAccess access; private final RoomMemberRepository members; private final RoomEventRepository events;
 private final RoomBanRepository bans; private final RoomInviteRepository invites; private final RoomRepository rooms; private final UserRepository users; private final ObjectMapper mapper;
 public record State(boolean locked,boolean ended){}
 public record Flag(boolean value){}
 public record RoleEdit(@NotNull RoomRole role){}
 public record InviteEdit(@Min(1) @Max(168) int hours,@Min(1) @Max(100) int maxUses){}
 public record Redeem(@NotBlank @Size(max=128) String token){}
 public record InviteView(Long id,OffsetDateTime expiresAt,int maxUses,int usedCount,boolean revoked){}
 public record InviteCreated(Long id,String token,OffsetDateTime expiresAt){}
 @Transactional(readOnly=true) public State get(Long room,Long user){var r=access.read(room,user).getRoom();return new State(r.isLocked(),r.getEndedAt()!=null);}
 private void event(RoomMember m,RoomEventType type,Map<String,Object> data){events.save(RoomEvent.builder().room(m.getRoom()).actor(m.getUser()).eventType(type).payload(mapper.valueToTree(data)).build());}
 public void lock(Long room,Long user,boolean value){var m=access.write(room,user);access.host(m);m.getRoom().setLocked(value);event(m,RoomEventType.REACTION,Map.of("action","LOCK","value",value));}
 public void end(Long room,Long user){var m=access.write(room,user);access.host(m);m.getRoom().end();event(m,RoomEventType.REACTION,Map.of("action","END"));}
 public void hand(Long room,Long user,boolean value){access.write(room,user).raiseHand(value);}
 public void role(Long room,Long user,Long target,RoomRole role){var m=access.write(room,user);access.host(m);var t=access.read(room,target);if(t.getRoleInRoom()==RoomRole.HOST||role==RoomRole.HOST)throw error(ErrorCode.BAD_REQUEST,"호스트 역할은 이 화면에서 변경할 수 없습니다.");t.changeRole(role);event(m,RoomEventType.ROLE_CHANGE,Map.of("target",target,"role",role));}
 public void mute(Long room,Long user,Long target,boolean value){var m=access.write(room,user);access.host(m);var t=access.read(room,target);if(t.getRoleInRoom()==RoomRole.HOST)throw error(ErrorCode.BAD_REQUEST,"호스트에게 적용할 수 없습니다.");t.setMuted(value);event(m,RoomEventType.MUTE,Map.of("target",target,"value",value));}
 public void kick(Long room,Long user,Long target){var m=access.write(room,user);access.host(m);var t=access.read(room,target);if(t.getRoleInRoom()==RoomRole.HOST)throw error(ErrorCode.BAD_REQUEST,"호스트는 내보낼 수 없습니다.");bans.save(new RoomBan(new RoomMemberId(room,target)));members.delete(t);event(m,RoomEventType.KICK,Map.of("target",target));}
 @Transactional(readOnly=true) public List<InviteView> invites(Long room,Long user){access.host(access.read(room,user));return invites.findByRoomIdOrderByIdDesc(room).stream().map(i->new InviteView(i.getId(),i.getExpiresAt(),i.getMaxUses(),i.getUsedCount(),i.isRevoked())).toList();}
 public InviteCreated invite(Long room,Long user,InviteEdit req){var m=access.write(room,user);access.host(m);byte[] bytes=new byte[32];new SecureRandom().nextBytes(bytes);String raw=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);var i=invites.save(RoomInvite.builder().room(m.getRoom()).createdBy(m.getUser()).token(hash(raw)).inviteType(InviteType.LINK).expiresAt(OffsetDateTime.now().plusHours(req.hours())).maxUses(req.maxUses()).usedCount(0).defaultRoleInRoom(RoomRole.MEMBER).revoked(false).build());return new InviteCreated(i.getId(),raw,i.getExpiresAt());}
 public void revoke(Long room,Long user,Long id){access.host(access.write(room,user));invites.findById(id).filter(i->Objects.equals(i.getRoom().getId(),room)).orElseThrow(()->error(ErrorCode.NOT_FOUND,"초대가 없습니다.")).revoke();}
 public void redeem(Long room,Long user,String token){var r=rooms.lockForBoard(room).orElseThrow(()->error(ErrorCode.NOT_FOUND,"회의실이 없습니다."));if(r.getEndedAt()!=null)throw error(ErrorCode.CONFLICT,"종료된 회의실입니다.");if(bans.existsById(new RoomMemberId(room,user)))throw error(ErrorCode.FORBIDDEN,"이 회의실에서 내보내진 계정입니다.");if(members.existsByIdRoomIdAndIdUserId(room,user))return;if(r.isLocked())throw error(ErrorCode.FORBIDDEN,"잠긴 회의실입니다.");var i=invites.findByToken(hash(token)).filter(v->Objects.equals(v.getRoom().getId(),room)&&!v.isRevoked()&&v.getInviteType()==InviteType.LINK&&v.getExpiresAt()!=null&&v.getExpiresAt().isAfter(OffsetDateTime.now())&&v.getUsedCount()<v.getMaxUses()).orElseThrow(()->error(ErrorCode.FORBIDDEN,"만료되었거나 사용할 수 없는 초대입니다."));var account=users.findById(user).orElseThrow(()->error(ErrorCode.FORBIDDEN,"계정을 확인하세요."));var m=members.save(RoomMember.builder().id(new RoomMemberId(room,user)).room(r).user(account).roleInRoom(RoomRole.MEMBER).joinedAt(OffsetDateTime.now()).muted(false).handRaised(false).build());i.consume();event(m,RoomEventType.JOIN,Map.of("reason","invite"));}
 public static String hash(String token){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));}catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
}
