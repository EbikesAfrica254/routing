package com.ebikes.routing.dtos.internal;

import com.ebikes.routing.enums.RouteComputationMode;
import com.ebikes.routing.enums.RoutingEngine;

public record RouteResult(
    int distanceMeters,
    int durationSeconds,
    String engineCostingUsed,
    boolean isFallback,
    RouteComputationMode routeComputationMode,
    RoutingEngine routingEngine) {}
