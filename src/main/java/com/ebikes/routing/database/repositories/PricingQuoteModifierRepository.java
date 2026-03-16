package com.ebikes.routing.database.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ebikes.routing.database.entities.PricingQuoteModifier;

public interface PricingQuoteModifierRepository extends JpaRepository<PricingQuoteModifier, UUID> {

  List<PricingQuoteModifier> findByPricingQuoteId(UUID quoteId);
}
