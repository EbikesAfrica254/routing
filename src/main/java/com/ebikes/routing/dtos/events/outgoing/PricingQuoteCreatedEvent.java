package com.ebikes.routing.dtos.events.outgoing;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.ebikes.routing.enums.OrderType;
import com.ebikes.routing.enums.VehicleClass;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PricingQuoteCreatedEvent(
    UUID branchId,
    boolean isFallback,
    BigDecimal finalAmount,
    String currency,
    OrderType orderType,
    UUID orderId,
    UUID organizationId,
    UUID pricingQuoteId,
    String pricingPlanVersion,
    OffsetDateTime quotedAt,
    String serviceReference,
    VehicleClass vehicleClass)
    implements Serializable {}
