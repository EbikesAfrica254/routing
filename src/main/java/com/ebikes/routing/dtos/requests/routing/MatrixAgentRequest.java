package com.ebikes.routing.dtos.requests.routing;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.ebikes.routing.dtos.internal.LocationDto;

public record MatrixAgentRequest(@NotNull UUID agentId, @NotNull @Valid LocationDto location) {}
