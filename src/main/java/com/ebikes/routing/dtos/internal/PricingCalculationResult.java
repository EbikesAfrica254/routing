package com.ebikes.routing.dtos.internal;

import java.math.BigDecimal;
import java.util.List;

import com.ebikes.routing.services.pricing.AppliedModifierSnapshot;

public record PricingCalculationResult(
    BigDecimal baseFareAmount,
    BigDecimal distanceChargeAmount,
    BigDecimal modifiersTotalAmount,
    BigDecimal finalAmount,
    List<AppliedModifierSnapshot> appliedModifiers) {}
