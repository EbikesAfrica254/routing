package com.ebikes.routing.dtos.requests.filters;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.ebikes.routing.dtos.requests.filters.bases.BaseFilter;
import com.ebikes.routing.enums.OrderType;
import com.ebikes.routing.enums.PricingQuoteStatus;
import com.ebikes.routing.enums.RouteComputationMode;
import com.ebikes.routing.enums.RoutingEngine;
import com.ebikes.routing.enums.VehicleClass;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PricingQuoteFilter extends BaseFilter {

  private UUID branchId;
  private OffsetDateTime createdAtAfter;
  private OffsetDateTime createdAtBefore;
  private BigDecimal finalAmountMax;
  private BigDecimal finalAmountMin;
  private Boolean isCommitted;
  private Boolean isFallback;
  private UUID orderId;
  private UUID organizationId;
  private UUID pricingPlanId;
  private String pricingPlanVersion;
  private RouteComputationMode routeComputationMode;
  private RoutingEngine routingEngine;
  private PricingQuoteStatus status;
  private OrderType orderType;
  private VehicleClass vehicleClass;
}
