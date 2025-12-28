package com.mingleroom.domain.bookmarks.repository;

import com.mingleroom.domain.bookmarks.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
}
