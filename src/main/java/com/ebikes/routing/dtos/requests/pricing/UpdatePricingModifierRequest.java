package com.ebikes.routing.dtos.requests.pricing;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import com.ebikes.routing.enums.OrderType;
import com.ebikes.routing.enums.VehicleClass;

public record UpdatePricingModifierRequest(
    OrderType appliesToOrderType,
    @NotNull @Valid ModifierConditionSetDto conditionSet,
    @NotNull OffsetDateTime effectiveFrom,
    OffsetDateTime effectiveTo,
    @Min(0) int priority,
    @NotNull @DecimalMin("0") BigDecimal valueAmount,
    VehicleClass vehicleClass) {}
