package com.ebikes.routing.dtos.requests.routing;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import com.ebikes.routing.dtos.internal.LocationDto;
import com.ebikes.routing.enums.VehicleClass;

public record CreateMatrixRequest(
    @NotNull @NotEmpty @Valid List<MatrixAgentRequest> agents,
    @NotNull @Valid LocationDto pickupLocation,
    @NotNull VehicleClass vehicleClass) {

  public CreateMatrixRequest {
    agents = agents != null ? List.copyOf(agents) : null;
  }
}
