package com.medikasentosa.features.content.repository;

import com.medikasentosa.features.content.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ContentRepository extends JpaRepository<Content, Long> {
    List<Content> findByPageKey(String pageKey);
    Optional<Content> findByPageKeyAndSectionKey(String pageKey, String sectionKey);
}