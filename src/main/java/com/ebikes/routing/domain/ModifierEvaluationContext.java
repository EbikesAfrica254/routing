package com.ebikes.routing.domain;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import com.ebikes.routing.enums.OrderType;
import com.ebikes.routing.enums.VehicleClass;

public record ModifierEvaluationContext(
    UUID branchId,
    UUID organizationId,
    OrderType orderType,
    VehicleClass vehicleClass,
    ZonedDateTime requestTime,
    ZoneId zoneId) {}
