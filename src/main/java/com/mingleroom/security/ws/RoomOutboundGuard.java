package com.mingleroom.security.ws;
import com.mingleroom.domain.room.members.repository.RoomMemberRepository;
import com.mingleroom.domain.room.rooms.repository.RoomRepository;
import com.mingleroom.security.config.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
@Component @RequiredArgsConstructor
public class RoomOutboundGuard implements ChannelInterceptor {
 private record Session(Long user,long expires){}
 private final ConcurrentHashMap<String,Session> sessions=new ConcurrentHashMap<>();
 private final RoomMemberRepository members; private final RoomRepository rooms;
 private static final Pattern DEST=Pattern.compile("^/sub/(?:chat|board|cursor|signal)/room/([1-9][0-9]*)(?:/user/[1-9][0-9]*)?$");
 @EventListener public void connected(SessionConnectedEvent event){var a=StompHeaderAccessor.wrap(event.getMessage());var connect=a.getHeader("simpConnectMessage");if(!(connect instanceof Message<?> message))return;var c=StompHeaderAccessor.wrap(message);if(!(event.getUser() instanceof Authentication auth)||!(auth.getPrincipal() instanceof UserPrincipal u))return;Object expiry=c.getSessionAttributes()==null?null:c.getSessionAttributes().get("mingleroom.jwtExpiresAt");if(expiry instanceof Long value&&a.getSessionId()!=null)sessions.put(a.getSessionId(),new Session(u.getId(),value));}
 @EventListener public void disconnected(SessionDisconnectEvent event){sessions.remove(event.getSessionId());}
 @Override public Message<?> preSend(Message<?> message,MessageChannel channel){var a=StompHeaderAccessor.wrap(message);if(a.getMessageType()!=org.springframework.messaging.simp.SimpMessageType.MESSAGE)return message;var match=DEST.matcher(a.getDestination()==null?"":a.getDestination());if(!match.matches())return null;var s=a.getSessionId()==null?null:sessions.get(a.getSessionId());if(s==null||s.expires()<=System.currentTimeMillis())return null;Long room;try{room=Long.valueOf(match.group(1));}catch(NumberFormatException e){return null;}if(!members.existsByIdRoomIdAndIdUserId(room,s.user())||rooms.findById(room).map(r->r.getEndedAt()!=null).orElse(true))return null;return message;}
}
