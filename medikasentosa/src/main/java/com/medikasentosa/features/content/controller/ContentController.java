package com.medikasentosa.features.content.controller;

import com.medikasentosa.features.content.service.ContentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/content")
public class ContentController {

    private final ContentService service;
    public ContentController(ContentService s) { this.service = s; }

    /** Public: get content for a page */
    @GetMapping("/{pageKey}")
    public Map<String, String> getContent(@PathVariable String pageKey) {
        return service.getPageContent(pageKey);
    }

    /** Admin: save content for a page */
    @PutMapping("/{pageKey}")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> saveContent(@PathVariable String pageKey, @RequestBody Map<String, String> sections) {
        return service.savePageContent(pageKey, sections);
    }

    /** List all pages */
    @GetMapping
    public List<String> getPages() { return service.getAllPages(); }
}