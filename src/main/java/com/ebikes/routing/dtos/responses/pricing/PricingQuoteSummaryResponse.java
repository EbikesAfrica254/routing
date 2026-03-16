package com.ebikes.routing.dtos.responses.pricing;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.ebikes.routing.dtos.internal.MoneyDto;
import com.ebikes.routing.enums.OrderType;
import com.ebikes.routing.enums.PricingQuoteStatus;
import com.ebikes.routing.enums.VehicleClass;

public record PricingQuoteSummaryResponse(
    UUID branchId,
    OffsetDateTime createdAt,
    MoneyDto finalAmount,
    UUID id,
    boolean isCommitted,
    boolean isFallback,
    UUID orderId,
    OrderType orderType,
    UUID organizationId,
    PricingQuoteStatus status,
    VehicleClass vehicleClass) {}
