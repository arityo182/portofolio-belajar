package com.medikasentosa.features.poli.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.medikasentosa.features.poli.entity.Poli;

import java.util.List;

public interface PoliRepository extends JpaRepository<Poli, Long> {

    List<Poli> findByIsActiveTrue();

    Page<Poli> findByIsActiveTrue(Pageable pageable);

    boolean existsByNamaIgnoreCase(String nama);
}
