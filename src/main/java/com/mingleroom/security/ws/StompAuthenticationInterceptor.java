package com.mingleroom.security.ws;

import com.mingleroom.domain.room.members.repository.RoomMemberRepository;
import com.mingleroom.domain.room.rooms.repository.RoomRepository;
import com.mingleroom.security.CustomUserDetailsService;
import com.mingleroom.security.config.UserPrincipal;
import com.mingleroom.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class StompAuthenticationInterceptor implements ChannelInterceptor {
    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;
    private final RoomMemberRepository members;
    private final RoomRepository rooms;
    private static final String EXPIRY = "mingleroom.jwtExpiresAt";
    private static final Pattern SEND = Pattern.compile("^/pub/(?:chat|cursor)/room/([1-9][0-9]*)$");
    private static final Pattern SUBSCRIBE = Pattern.compile("^/sub/(?:chat|board|cursor)/room/([1-9][0-9]*)$");

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        var accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) return message;
        if (accessor.getCommand() == StompCommand.CONNECT) {
            String header = accessor.getFirstNativeHeader("Authorization");
            if (header == null || !header.startsWith("Bearer ")) throw denied();
            try {
                var claims = jwtProvider.parse(header.substring(7).trim()).getPayload();
                var user = (UserPrincipal) userDetailsService.loadUserByUsername(claims.getSubject());
                if (!user.isEnabled() || !user.isAccountNonLocked() || claims.getExpiration() == null) throw denied();
                accessor.setUser(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
                if (accessor.getSessionAttributes() == null) accessor.setSessionAttributes(new HashMap<>());
                accessor.getSessionAttributes().put(EXPIRY, claims.getExpiration().getTime());
            } catch (RuntimeException ex) {
                throw denied();
            }
        } else if (accessor.getCommand() == StompCommand.SEND || accessor.getCommand() == StompCommand.SUBSCRIBE) {
            if (!(accessor.getUser() instanceof Authentication auth)
                    || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserPrincipal user)) throw denied();
            Object expiry = accessor.getSessionAttributes() == null ? null : accessor.getSessionAttributes().get(EXPIRY);
            if (!(expiry instanceof Long value) || value <= System.currentTimeMillis()) throw denied();
            var pattern = accessor.getCommand() == StompCommand.SEND ? SEND : SUBSCRIBE;
            var matcher = pattern.matcher(accessor.getDestination() == null ? "" : accessor.getDestination());
            if (!matcher.matches()) throw denied();
            Long roomId;
            try { roomId = Long.valueOf(matcher.group(1)); } catch (NumberFormatException ex) { throw denied(); }
            if (!members.existsByIdRoomIdAndIdUserId(roomId, user.getId())) throw denied();
            if (rooms.findById(roomId).map(room -> room.getEndedAt() != null).orElse(true)) throw denied();
        }
        return message;
    }

    private AccessDeniedException denied() {
        return new AccessDeniedException("로그인 상태와 회의실 참여 권한을 확인하세요.");
    }
}
