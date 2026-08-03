package com.medikasentosa.features.content.service;

import com.medikasentosa.features.content.entity.Content;
import com.medikasentosa.features.content.repository.ContentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ContentService {

    private static final Logger log = LoggerFactory.getLogger(ContentService.class);

    private final ContentRepository repo;
    public ContentService(ContentRepository r) { this.repo = r; }

    /** Get all sections for a page as key-value map. */
    public Map<String, String> getPageContent(String pageKey) {
        Map<String, String> map = new HashMap<>();
        repo.findByPageKey(pageKey).forEach(c -> map.put(c.getSectionKey(), c.getValue()));
        return map;
    }

    /** Save or update multiple sections for a page at once. */
    @Transactional
    public Map<String, String> savePageContent(String pageKey, Map<String, String> sections) {
        log.info("Saving page content for pageKey={} with {} sections", pageKey, sections.size());
        sections.forEach((key, value) -> {
            Content c = repo.findByPageKeyAndSectionKey(pageKey, key)
                    .orElse(Content.builder().pageKey(pageKey).sectionKey(key).build());
            c.setValue(value != null ? value : "");
            repo.save(c);
        });
        return getPageContent(pageKey);
    }

    /** Get all pages (for admin list). */
    public List<String> getAllPages() {
        return List.of("beranda", "layanan", "dokter", "tentang", "kontak");
    }
}