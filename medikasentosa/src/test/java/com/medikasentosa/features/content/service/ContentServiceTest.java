package com.medikasentosa.features.content.service;

import com.medikasentosa.features.content.entity.Content;
import com.medikasentosa.features.content.repository.ContentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContentServiceTest {

    @Mock
    private ContentRepository repo;

    @InjectMocks
    private ContentService contentService;

    private Content heroContent;
    private Content aboutContent;

    @BeforeEach
    void setUp() {
        heroContent = Content.builder()
                .id(1L)
                .pageKey("beranda")
                .sectionKey("hero_title")
                .value("Selamat Datang di RS Medika Sentosa")
                .build();

        aboutContent = Content.builder()
                .id(2L)
                .pageKey("beranda")
                .sectionKey("about_text")
                .value("RS Medika Sentosa melayani dengan sepenuh hati sejak 1998")
                .build();
    }

    @Test
    void getPageContent_returnsKeyValueMap() {
        when(repo.findByPageKey("beranda")).thenReturn(List.of(heroContent, aboutContent));

        Map<String, String> result = contentService.getPageContent("beranda");

        assertThat(result).hasSize(2);
        assertThat(result.get("hero_title")).isEqualTo("Selamat Datang di RS Medika Sentosa");
        assertThat(result.get("about_text")).isEqualTo("RS Medika Sentosa melayani dengan sepenuh hati sejak 1998");
    }

    @Test
    void savePageContent_savesNewAndUpdatesExisting() {
        Map<String, String> sections = Map.of(
                "hero_title", "Selamat Datang di RS Medika Sentosa",
                "visi", "Menjadi rumah sakit terbaik di Indonesia"
        );
        when(repo.findByPageKeyAndSectionKey("beranda", "hero_title"))
                .thenReturn(Optional.of(heroContent));
        when(repo.findByPageKeyAndSectionKey("beranda", "visi"))
                .thenReturn(Optional.empty());
        when(repo.save(any(Content.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repo.findByPageKey("beranda")).thenReturn(List.of(heroContent,
                Content.builder().pageKey("beranda").sectionKey("visi")
                        .value("Menjadi rumah sakit terbaik di Indonesia").build()));

        Map<String, String> result = contentService.savePageContent("beranda", sections);

        assertThat(result.get("visi")).isEqualTo("Menjadi rumah sakit terbaik di Indonesia");
        verify(repo, times(2)).save(any(Content.class));
    }

    @Test
    void getAllPages_returnsDefaultPageKeys() {
        List<String> pages = contentService.getAllPages();

        assertThat(pages).contains("beranda", "layanan", "dokter", "tentang", "kontak");
    }
}
