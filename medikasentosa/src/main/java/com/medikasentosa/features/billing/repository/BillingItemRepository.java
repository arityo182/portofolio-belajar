package com.medikasentosa.features.billing.repository;

import com.medikasentosa.features.billing.entity.BillingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/** Repository for {@link BillingItem}. */
public interface BillingItemRepository extends JpaRepository<BillingItem, Long> {
    List<BillingItem> findByBillingId(Long billingId);
}