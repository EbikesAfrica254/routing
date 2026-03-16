package com.ebikes.routing.dtos.responses.routing;

import java.util.UUID;

public record MatrixEntryResponse(
    UUID agentId, int distanceMeters, int durationSeconds, boolean isFallback) {}
