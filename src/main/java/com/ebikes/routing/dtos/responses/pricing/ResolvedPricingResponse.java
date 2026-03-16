package com.ebikes.routing.dtos.responses.pricing;

import java.util.List;
import java.util.UUID;

import com.ebikes.routing.enums.ScopeType;
import com.ebikes.routing.enums.VehicleClass;

public record ResolvedPricingResponse(
    List<PricingModifierSummaryResponse> applicableModifiers,
    ScopeType resolvedAtScope,
    UUID resolvedScopeId,
    PricingPlanDetailResponse resolvedPlan,
    VehicleClass vehicleClass) {}
