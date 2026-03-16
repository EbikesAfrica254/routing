package com.ebikes.routing.dtos.responses.pricing;

import java.util.UUID;

import com.ebikes.routing.dtos.internal.MoneyDto;
import com.ebikes.routing.enums.ModifierStackingMode;
import com.ebikes.routing.enums.PricingModifierType;

public record AppliedModifierResponse(
    MoneyDto appliedAmount,
    PricingModifierType modifierType,
    String modifierVersion,
    UUID pricingModifierId,
    int priority,
    ModifierStackingMode stackingMode,
    MoneyDto valueAmount) {}
