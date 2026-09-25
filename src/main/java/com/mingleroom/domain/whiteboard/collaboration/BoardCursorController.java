package com.mingleroom.domain.whiteboard.collaboration;
import com.mingleroom.security.config.UserPrincipal;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import java.security.Principal;
import static com.mingleroom.domain.whiteboard.collaboration.BoardModels.*;
@Controller
public class BoardCursorController {
    @MessageMapping("/cursor/room/{roomId}") @SendTo("/sub/cursor/room/{roomId}")
    public Cursor cursor(@DestinationVariable Long roomId,CursorInput input,Principal principal){
        if(!(principal instanceof Authentication a)||!(a.getPrincipal() instanceof UserPrincipal u))throw new AccessDeniedException("로그인이 필요합니다.");
        if(input==null||input.x()==null||input.y()==null||!Double.isFinite(input.x())||!Double.isFinite(input.y())||input.x()<0||input.x()>1280||input.y()<0||input.y()>850)throw new IllegalArgumentException("잘못된 커서 좌표입니다.");
        return new Cursor(u.getId(),u.getDisplayName(),input.x(),input.y());
    }
}
