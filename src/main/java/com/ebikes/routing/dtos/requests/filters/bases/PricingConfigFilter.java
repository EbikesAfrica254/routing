package com.ebikes.routing.dtos.requests.filters.bases;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.ebikes.routing.enums.PricingStatus;
import com.ebikes.routing.enums.ScopeType;
import com.ebikes.routing.enums.VehicleClass;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public abstract class PricingConfigFilter extends BaseFilter {

  private OffsetDateTime activatedAtAfter;
  private OffsetDateTime activatedAtBefore;
  private OffsetDateTime archivedAtAfter;
  private OffsetDateTime archivedAtBefore;
  private OffsetDateTime createdAtAfter;
  private OffsetDateTime createdAtBefore;
  private OffsetDateTime effectiveFromAfter;
  private OffsetDateTime effectiveFromBefore;
  private OffsetDateTime effectiveToAfter;
  private OffsetDateTime effectiveToBefore;
  private UUID scopeId;
  private ScopeType scopeType;
  private PricingStatus status;
  private VehicleClass vehicleClass;
  private String version;
}
