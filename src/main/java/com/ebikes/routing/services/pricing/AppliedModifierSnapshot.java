package com.ebikes.routing.services.pricing;

import java.math.BigDecimal;
import java.util.UUID;

import com.ebikes.routing.enums.PricingModifierType;

public record AppliedModifierSnapshot(
    UUID modifierId,
    String modifierVersion,
    PricingModifierType modifierType,
    BigDecimal valueAmount,
    BigDecimal appliedAmount) {}
