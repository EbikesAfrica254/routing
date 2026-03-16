package com.ebikes.routing.dtos.requests.pricing;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record UpdatePricingPlanRequest(
    @NotNull @DecimalMin("0") BigDecimal baseFareAmount,
    @NotNull OffsetDateTime effectiveFrom,
    OffsetDateTime effectiveTo,
    @DecimalMin("0") BigDecimal maximumChargeAmount,
    @NotNull @DecimalMin("0") BigDecimal minimumChargeAmount,
    @NotNull @DecimalMin("0") BigDecimal perKmRateAmount) {}
