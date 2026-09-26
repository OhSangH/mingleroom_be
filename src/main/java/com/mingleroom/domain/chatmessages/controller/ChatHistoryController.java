package com.mingleroom.domain.chatmessages.controller;
import com.mingleroom.domain.chatmessages.dto.ChatHistoryRes;
import com.mingleroom.domain.chatmessages.service.ChatService;
import com.mingleroom.security.config.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController
@RequiredArgsConstructor
@RequestMapping("/room/{roomId}/messages")
public class ChatHistoryController {
    private final ChatService service;
    @GetMapping public ChatHistoryRes history(@PathVariable Long roomId,@RequestParam(required=false) String before,
        @RequestParam(required=false) String after,@RequestParam(defaultValue="50") int limit,@AuthenticationPrincipal UserPrincipal user){
        return service.history(roomId,user.getId(),before,after,limit);
    }
    @GetMapping("/search")
    public ChatHistoryRes search(@PathVariable Long roomId,@AuthenticationPrincipal UserPrincipal user,@RequestParam String q,@RequestParam(required=false) String before){return service.search(roomId,user.getId(),q,before);}
}
