package com.ebikes.routing.dtos.responses.pricing;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.ebikes.routing.enums.ModifierStackingMode;
import com.ebikes.routing.enums.OrderType;
import com.ebikes.routing.enums.PricingModifierType;
import com.ebikes.routing.enums.PricingStatus;
import com.ebikes.routing.enums.ScopeType;
import com.ebikes.routing.enums.VehicleClass;

public record PricingModifierSummaryResponse(
    OrderType appliesToOrderType,
    OffsetDateTime effectiveFrom,
    OffsetDateTime effectiveTo,
    UUID id,
    PricingModifierType modifierType,
    int priority,
    UUID scopeId,
    ScopeType scopeType,
    ModifierStackingMode stackingMode,
    PricingStatus status,
    BigDecimal valueAmount,
    VehicleClass vehicleClass,
    String version) {}
