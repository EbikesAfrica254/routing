package com.ebikes.routing.dtos.requests.pricing;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.ebikes.routing.enums.ScopeType;
import com.ebikes.routing.enums.VehicleClass;

public record CreatePricingPlanRequest(
    @NotNull @DecimalMin("0") BigDecimal baseFareAmount,
    @NotNull OffsetDateTime effectiveFrom,
    OffsetDateTime effectiveTo,
    @DecimalMin("0") BigDecimal maximumChargeAmount,
    @NotNull @DecimalMin("0") BigDecimal minimumChargeAmount,
    @NotNull @DecimalMin("0") BigDecimal perKmRateAmount,
    UUID scopeId,
    @NotNull ScopeType scopeType,
    @NotNull VehicleClass vehicleClass,
    @NotBlank @Size(max = 100) String version) {}
