package com.ebikes.routing.dtos.requests.pricing;

import java.time.DayOfWeek;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import com.ebikes.routing.enums.OrderType;
import com.ebikes.routing.enums.VehicleClass;

public record ModifierConditionSetDto(
    UUID branchId,
    Set<DayOfWeek> daysOfWeek,
    @Min(0) @Max(23) Integer endHour,
    UUID organizationId,
    OrderType orderType,
    @Min(0) @Max(23) Integer startHour,
    VehicleClass vehicleClass) {

  public ModifierConditionSetDto {
    daysOfWeek = daysOfWeek != null ? Set.copyOf(daysOfWeek) : null;
  }
}
