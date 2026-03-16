package com.ebikes.routing.domain;

import java.io.Serial;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;

import com.ebikes.routing.enums.OrderType;
import com.ebikes.routing.enums.VehicleClass;

public record ConditionSet(
    UUID branchId,
    Set<DayOfWeek> daysOfWeek,
    Integer endHour,
    UUID organizationId,
    OrderType orderType,
    Integer startHour,
    VehicleClass vehicleClass)
    implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  public ConditionSet {
    if (startHour != null && endHour != null && startHour >= endHour) {
      throw new IllegalArgumentException("startHour must be strictly less than endHour");
    }
    daysOfWeek = daysOfWeek != null ? Set.copyOf(daysOfWeek) : null;
  }

  public boolean matches(ModifierEvaluationContext context) {
    if (branchId != null && !branchId.equals(context.branchId())) {
      return false;
    }

    if (organizationId != null && !organizationId.equals(context.organizationId())) {
      return false;
    }

    if (orderType != null && !orderType.equals(context.orderType())) {
      return false;
    }

    if (vehicleClass != null && !vehicleClass.equals(context.vehicleClass())) {
      return false;
    }

    if (daysOfWeek != null && !daysOfWeek.isEmpty()) {
      ZonedDateTime localTime = context.requestTime().withZoneSameInstant(context.zoneId());
      if (!daysOfWeek.contains(localTime.getDayOfWeek())) {
        return false;
      }
    }

    if (startHour != null && endHour != null) {
      ZonedDateTime localTime = context.requestTime().withZoneSameInstant(context.zoneId());
      int hour = localTime.getHour();
      return hour >= startHour && hour < endHour;
    }

    return true;
  }
}
