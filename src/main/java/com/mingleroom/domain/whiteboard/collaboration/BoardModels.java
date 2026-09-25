package com.mingleroom.domain.whiteboard.collaboration;
import jakarta.validation.constraints.*;
import java.util.List;
public final class BoardModels {
    private BoardModels() {}
    public record Sticky(String id, String text, String color, double x, double y, Long authorId, String author, int revision) {}
    public record State(String schema, int version, List<Sticky> notes) {}
    public record Edit(@NotNull @Min(0) Integer revision, @NotBlank @Size(max=2000) String text,
        @NotNull @Pattern(regexp="yellow|purple|green|pink") String color,
        @NotNull @DecimalMin("0") @DecimalMax("1040") Double x,
        @NotNull @DecimalMin("0") @DecimalMax("650") Double y) {}
    public record CursorInput(Double x, Double y) {}
    public record Cursor(Long userId, String name, double x, double y) {}
}
