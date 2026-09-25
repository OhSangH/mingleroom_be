package com.mingleroom.domain.whiteboard.collaboration;
import com.mingleroom.security.config.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import static com.mingleroom.domain.whiteboard.collaboration.BoardModels.*;
@RestController
@RequiredArgsConstructor
@RequestMapping("/room/{roomId}/board")
public class BoardController {
    private final BoardService service;
    @GetMapping public State get(@PathVariable Long roomId,@AuthenticationPrincipal UserPrincipal user){return service.get(roomId,user.getId());}
    @PutMapping("/notes/{id}") public State save(@PathVariable Long roomId,@PathVariable String id,@Valid @RequestBody Edit edit,@AuthenticationPrincipal UserPrincipal user){return service.edit(roomId,user.getId(),id,edit,null);}
    @DeleteMapping("/notes/{id}") public State delete(@PathVariable Long roomId,@PathVariable String id,@RequestParam Integer revision,@AuthenticationPrincipal UserPrincipal user){return service.edit(roomId,user.getId(),id,null,revision);}
}
