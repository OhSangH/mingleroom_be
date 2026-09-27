package com.mingleroom.domain.room.rooms.dto;
import com.mingleroom.common.enums.RoomVisibility;
import jakarta.validation.constraints.*;
public record RoomUpdateReq(
 @NotBlank @Size(max=150) String title,
 @NotNull RoomVisibility visibility,
 @NotBlank @Size(max=150) String expectedTitle,
 @NotNull RoomVisibility expectedVisibility
) {}
