package com.ebikes.routing.dtos.requests.routing;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.ebikes.routing.dtos.internal.LocationDto;
import com.ebikes.routing.enums.OrderType;
import com.ebikes.routing.enums.VehicleClass;

public record CreateQuoteRequest(
    UUID branchId,
    @NotNull Boolean commitQuote,
    @NotNull @Valid LocationDto destination,
    UUID orderId,
    UUID organizationId,
    @NotNull OrderType orderType,
    @NotNull @Valid LocationDto origin,
    @NotNull VehicleClass vehicleClass) {}
