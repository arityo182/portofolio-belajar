package com.medikasentosa.features.content.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity untuk menyimpan konten dinamis per halaman (CMS).
 * Setiap baris = satu section dari satu halaman.
 *
 * @author Ari
 * @since 1.0.0
 */
@Entity
@Table(name = "content", uniqueConstraints = @UniqueConstraint(columnNames = {"page_key", "section_key"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Content {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Halaman target: "beranda", "layanan", "dokter", "tentang", "kontak" */
    @Column(name = "page_key", nullable = false)
    private String pageKey;

    /** Section/field: "hero_title", "hero_subtitle", "about_text", dll */
    @Column(name = "section_key", nullable = false)
    private String sectionKey;

    /** Isi konten */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String value;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist @PreUpdate
    void onUpdate() { this.updatedAt = LocalDateTime.now(); }
}