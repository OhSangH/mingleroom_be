package com.mingleroom.domain.collaboration;
import jakarta.validation.constraints.*;
import com.mingleroom.common.enums.ActionStatus;
import java.time.*;
import java.util.List;
public final class MeetingModels {
 public record NoteEdit(@NotNull @Size(max=50000) String content,@Min(0) int version){}
 public record NoteView(String content,int version,String author,OffsetDateTime updatedAt){}
 public record TaskEdit(@NotBlank @Size(max=200) String title,@Size(max=10000) String description,Long assigneeId,LocalDate dueDate,@NotNull ActionStatus status,@Min(0) int revision){}
 public record TaskView(Long id,String title,String description,Long assigneeId,String assignee,LocalDate dueDate,ActionStatus status,int revision){}
 public record PollEdit(@NotBlank @Size(max=255) String question,@NotNull @Size(min=2,max=8) List<@NotBlank @Size(max=150) String> options){}
 public record OptionView(Long id,String label,long count){}
 public record PollView(Long id,String question,boolean closed,Long myVote,List<OptionView> options){}
 public record Vote(@NotNull @Positive Long optionId){}
 public record MarkEdit(@NotBlank @Size(max=120) String label){}
 public record MarkView(Long id,String label,int atMs,Long authorId,String author){}
 public record Summary(NoteView note,List<TaskView> tasks,List<PollView> polls,List<MarkView> bookmarks){}
}
