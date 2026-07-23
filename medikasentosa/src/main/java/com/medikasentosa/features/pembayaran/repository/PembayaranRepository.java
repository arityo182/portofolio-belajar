package com.medikasentosa.features.pembayaran.repository;

import com.medikasentosa.features.pembayaran.entity.Pembayaran;
import com.medikasentosa.features.pembayaran.entity.StatusPembayaran;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PembayaranRepository extends JpaRepository<Pembayaran, Long> {
    Optional<Pembayaran> findByBillingId(Long billingId);
    List<Pembayaran> findByStatus(StatusPembayaran status);
}