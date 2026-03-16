package com.ebikes.routing.dtos.events.outgoing;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.ebikes.routing.enums.ScopeType;
import com.ebikes.routing.enums.VehicleClass;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PricingPlanActivatedEvent(
    OffsetDateTime activatedAt,
    OffsetDateTime effectiveFrom,
    UUID pricingPlanId,
    String serviceReference,
    UUID scopeId,
    ScopeType scopeType,
    String version,
    VehicleClass vehicleClass)
    implements Serializable {}
