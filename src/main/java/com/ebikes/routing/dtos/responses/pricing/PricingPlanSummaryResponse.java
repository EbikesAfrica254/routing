package com.ebikes.routing.dtos.responses.pricing;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.ebikes.routing.enums.PricingStatus;
import com.ebikes.routing.enums.ScopeType;
import com.ebikes.routing.enums.VehicleClass;

public record PricingPlanSummaryResponse(
    BigDecimal baseFareAmount,
    OffsetDateTime effectiveFrom,
    OffsetDateTime effectiveTo,
    UUID id,
    BigDecimal minimumChargeAmount,
    BigDecimal perKmRateAmount,
    UUID scopeId,
    ScopeType scopeType,
    PricingStatus status,
    VehicleClass vehicleClass,
    String version) {}
