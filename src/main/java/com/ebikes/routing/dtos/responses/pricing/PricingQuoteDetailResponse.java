package com.ebikes.routing.dtos.responses.pricing;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.ebikes.routing.dtos.internal.LocationDto;
import com.ebikes.routing.dtos.internal.MoneyDto;
import com.ebikes.routing.enums.OrderType;
import com.ebikes.routing.enums.PricingQuoteStatus;
import com.ebikes.routing.enums.RouteComputationMode;
import com.ebikes.routing.enums.RoutingEngine;
import com.ebikes.routing.enums.VehicleClass;

public record PricingQuoteDetailResponse(
    List<AppliedModifierResponse> appliedModifiers,
    MoneyDto baseFare,
    UUID branchId,
    OffsetDateTime createdAt,
    LocationDto destination,
    MoneyDto distanceCharge,
    int distanceMeters,
    int durationSeconds,
    String engineCostingUsed,
    OffsetDateTime expiresAt,
    MoneyDto finalAmount,
    UUID id,
    boolean isCommitted,
    boolean isFallback,
    UUID orderId,
    OrderType orderType,
    UUID organizationId,
    LocationDto origin,
    UUID pricingPlanId,
    String pricingPlanVersion,
    RouteComputationMode routeComputationMode,
    RoutingEngine routingEngine,
    PricingQuoteStatus status,
    VehicleClass vehicleClass) {}
