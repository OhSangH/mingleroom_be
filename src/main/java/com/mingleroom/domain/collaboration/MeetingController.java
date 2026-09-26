package com.mingleroom.domain.collaboration;
import com.mingleroom.security.config.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import static com.mingleroom.domain.collaboration.MeetingModels.*;
@RestController @RequiredArgsConstructor @RequestMapping("/room/{roomId}/meeting")
public class MeetingController {
 private final MeetingService service;
 @GetMapping public Summary get(@PathVariable Long roomId,@AuthenticationPrincipal UserPrincipal u){return service.get(roomId,u.getId());}
 @PutMapping("/note") public NoteView note(@PathVariable Long roomId,@AuthenticationPrincipal UserPrincipal u,@Valid @RequestBody NoteEdit req){return service.saveNote(roomId,u.getId(),req);}
 @PostMapping("/tasks") public TaskView create(@PathVariable Long roomId,@AuthenticationPrincipal UserPrincipal u,@Valid @RequestBody TaskEdit req){return service.saveTask(roomId,u.getId(),null,req);}
 @PutMapping("/tasks/{id}") public TaskView update(@PathVariable Long roomId,@PathVariable Long id,@AuthenticationPrincipal UserPrincipal u,@Valid @RequestBody TaskEdit req){return service.saveTask(roomId,u.getId(),id,req);}
 @DeleteMapping("/tasks/{id}") public void delete(@PathVariable Long roomId,@PathVariable Long id,@RequestParam int revision,@AuthenticationPrincipal UserPrincipal u){service.deleteTask(roomId,u.getId(),id,revision);}
 @PostMapping("/polls") public PollView poll(@PathVariable Long roomId,@AuthenticationPrincipal UserPrincipal u,@Valid @RequestBody PollEdit req){return service.createPoll(roomId,u.getId(),req);}
 @PutMapping("/polls/{id}/vote") public PollView vote(@PathVariable Long roomId,@PathVariable Long id,@AuthenticationPrincipal UserPrincipal u,@Valid @RequestBody Vote req){return service.vote(roomId,u.getId(),id,req.optionId());}
 @PostMapping("/polls/{id}/close") public PollView close(@PathVariable Long roomId,@PathVariable Long id,@AuthenticationPrincipal UserPrincipal u){return service.closePoll(roomId,u.getId(),id);}
 @PostMapping("/bookmarks") public void bookmark(@PathVariable Long roomId,@AuthenticationPrincipal UserPrincipal u,@Valid @RequestBody MarkEdit req){service.bookmark(roomId,u.getId(),req);}
 @DeleteMapping("/bookmarks/{id}") public void deleteMark(@PathVariable Long roomId,@PathVariable Long id,@AuthenticationPrincipal UserPrincipal u){service.deleteBookmark(roomId,u.getId(),id);}
}
