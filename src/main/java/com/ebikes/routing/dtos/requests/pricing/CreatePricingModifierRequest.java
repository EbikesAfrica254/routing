package com.ebikes.routing.dtos.requests.pricing;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.ebikes.routing.enums.ModifierStackingMode;
import com.ebikes.routing.enums.OrderType;
import com.ebikes.routing.enums.PricingModifierType;
import com.ebikes.routing.enums.ScopeType;
import com.ebikes.routing.enums.VehicleClass;

public record CreatePricingModifierRequest(
    OrderType appliesToOrderType,
    @NotNull @Valid ModifierConditionSetDto conditionSet,
    @NotNull OffsetDateTime effectiveFrom,
    OffsetDateTime effectiveTo,
    @NotNull PricingModifierType modifierType,
    @Min(0) int priority,
    UUID scopeId,
    @NotNull ScopeType scopeType,
    @NotNull ModifierStackingMode stackingMode,
    @NotNull @DecimalMin("0") BigDecimal valueAmount,
    VehicleClass vehicleClass,
    @NotBlank @Size(max = 100) String version) {}
