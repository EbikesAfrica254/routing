package com.ebikes.routing.dtos.responses.routing;

import java.util.List;

import com.ebikes.routing.dtos.internal.LocationDto;
import com.ebikes.routing.enums.VehicleClass;

public record MatrixResponse(
    LocationDto pickupLocation, List<MatrixEntryResponse> results, VehicleClass vehicleClass) {}
