package com.medikasentosa.features.billing.repository;

import com.medikasentosa.features.billing.entity.Billing;
import com.medikasentosa.features.billing.entity.StatusBilling;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/** Repository for {@link Billing}. */
public interface BillingRepository extends JpaRepository<Billing, Long> {
    List<Billing> findByPasienId(Long pasienId);
    List<Billing> findByStatus(StatusBilling status);
}