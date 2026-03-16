package com.ebikes.routing.dtos.responses.pricing;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.ebikes.routing.enums.PricingStatus;
import com.ebikes.routing.enums.ScopeType;
import com.ebikes.routing.enums.VehicleClass;

public record PricingPlanDetailResponse(
    OffsetDateTime activatedAt,
    String activatedBy,
    OffsetDateTime archivedAt,
    String archivedBy,
    BigDecimal baseFareAmount,
    OffsetDateTime createdAt,
    String createdBy,
    OffsetDateTime effectiveFrom,
    OffsetDateTime effectiveTo,
    UUID id,
    BigDecimal maximumChargeAmount,
    BigDecimal minimumChargeAmount,
    BigDecimal perKmRateAmount,
    UUID scopeId,
    ScopeType scopeType,
    PricingStatus status,
    VehicleClass vehicleClass,
    String version) {}
