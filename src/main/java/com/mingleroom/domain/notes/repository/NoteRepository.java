package com.mingleroom.domain.notes.repository;

import com.mingleroom.domain.notes.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, Long> {
}
