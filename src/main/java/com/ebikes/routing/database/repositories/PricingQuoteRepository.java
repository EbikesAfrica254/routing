package com.ebikes.routing.database.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ebikes.routing.database.entities.PricingQuote;

public interface PricingQuoteRepository
    extends JpaRepository<PricingQuote, UUID>, JpaSpecificationExecutor<PricingQuote> {

  @Query(
      """
      SELECT q FROM PricingQuote q
      WHERE q.orderId = :orderId
      AND q.isCommitted = true
      """)
  Optional<PricingQuote> findCommittedByOrderId(@Param("orderId") UUID orderId);
}
