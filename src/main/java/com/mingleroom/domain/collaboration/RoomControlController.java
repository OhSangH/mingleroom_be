package com.mingleroom.domain.collaboration;
import com.mingleroom.security.config.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import static com.mingleroom.domain.collaboration.RoomControls.*;
@RestController @RequiredArgsConstructor @RequestMapping("/room/{roomId}/controls")
public class RoomControlController {
 private final RoomControls service;
 @GetMapping public State state(@PathVariable Long roomId,@AuthenticationPrincipal UserPrincipal u){return service.get(roomId,u.getId());}
 @PutMapping("/lock") public void lock(@PathVariable Long roomId,@AuthenticationPrincipal UserPrincipal u,@RequestBody Flag req){service.lock(roomId,u.getId(),req.value());}
 @PostMapping("/end") public void end(@PathVariable Long roomId,@AuthenticationPrincipal UserPrincipal u){service.end(roomId,u.getId());}
 @PutMapping("/hand") public void hand(@PathVariable Long roomId,@AuthenticationPrincipal UserPrincipal u,@RequestBody Flag req){service.hand(roomId,u.getId(),req.value());}
 @PutMapping("/members/{id}/role") public void role(@PathVariable Long roomId,@PathVariable Long id,@AuthenticationPrincipal UserPrincipal u,@Valid @RequestBody RoleEdit req){service.role(roomId,u.getId(),id,req.role());}
 @PutMapping("/members/{id}/mute") public void mute(@PathVariable Long roomId,@PathVariable Long id,@AuthenticationPrincipal UserPrincipal u,@RequestBody Flag req){service.mute(roomId,u.getId(),id,req.value());}
 @DeleteMapping("/members/{id}") public void kick(@PathVariable Long roomId,@PathVariable Long id,@AuthenticationPrincipal UserPrincipal u){service.kick(roomId,u.getId(),id);}
 @GetMapping("/invites") public List<InviteView> invites(@PathVariable Long roomId,@AuthenticationPrincipal UserPrincipal u){return service.invites(roomId,u.getId());}
 @PostMapping("/invites") public InviteCreated invite(@PathVariable Long roomId,@AuthenticationPrincipal UserPrincipal u,@Valid @RequestBody InviteEdit req){return service.invite(roomId,u.getId(),req);}
 @DeleteMapping("/invites/{id}") public void revoke(@PathVariable Long roomId,@PathVariable Long id,@AuthenticationPrincipal UserPrincipal u){service.revoke(roomId,u.getId(),id);}
 @PostMapping("/redeem") public void redeem(@PathVariable Long roomId,@AuthenticationPrincipal UserPrincipal u,@Valid @RequestBody Redeem req){service.redeem(roomId,u.getId(),req.token());}
}
