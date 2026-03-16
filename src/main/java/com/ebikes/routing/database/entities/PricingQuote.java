package com.ebikes.routing.database.entities;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import com.ebikes.routing.database.entities.bases.BaseEntity;
import com.ebikes.routing.enums.OrderType;
import com.ebikes.routing.enums.PricingQuoteStatus;
import com.ebikes.routing.enums.RouteComputationMode;
import com.ebikes.routing.enums.RoutingEngine;
import com.ebikes.routing.enums.VehicleClass;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "pricing_quotes", schema = "routing")
public class PricingQuote extends BaseEntity {

  private static final String MVP_CURRENCY = "KES";

  @Column(name = "base_fare_amount", nullable = false, precision = 19, scale = 4)
  @NotNull private BigDecimal baseFareAmount;

  @Column(name = "branch_id")
  private UUID branchId;

  @Column(name = "currency", nullable = false, length = 3)
  @NotNull private String currency;

  @Column(name = "destination_latitude", nullable = false, precision = 9, scale = 6)
  @NotNull private BigDecimal destinationLatitude;

  @Column(name = "destination_longitude", nullable = false, precision = 9, scale = 6)
  @NotNull private BigDecimal destinationLongitude;

  @Column(name = "distance_charge_amount", nullable = false, precision = 19, scale = 4)
  @NotNull private BigDecimal distanceChargeAmount;

  @Column(name = "distance_meters", nullable = false)
  private int distanceMeters;

  @Column(name = "duration_seconds", nullable = false)
  private int durationSeconds;

  @Column(name = "engine_costing_used", nullable = false, length = 50)
  @NotNull private String engineCostingUsed;

  @Column(name = "expires_at", columnDefinition = "TIMESTAMPTZ")
  private OffsetDateTime expiresAt;

  @Column(name = "final_amount", nullable = false, precision = 19, scale = 4)
  @NotNull private BigDecimal finalAmount;

  @Column(name = "is_committed", nullable = false)
  private boolean isCommitted;

  @Column(name = "is_fallback", nullable = false)
  private boolean isFallback;

  @OneToMany(
      mappedBy = "pricingQuote",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private final List<PricingQuoteModifier> modifierBreakdown = new ArrayList<>();

  @Column(name = "modifiers_total_amount", nullable = false, precision = 19, scale = 4)
  @NotNull private BigDecimal modifiersTotalAmount;

  @Column(name = "order_id")
  private UUID orderId;

  @Column(name = "order_type", nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  @NotNull private OrderType orderType;

  @Column(name = "organization_id")
  private UUID organizationId;

  @Column(name = "origin_latitude", nullable = false, precision = 9, scale = 6)
  @NotNull private BigDecimal originLatitude;

  @Column(name = "origin_longitude", nullable = false, precision = 9, scale = 6)
  @NotNull private BigDecimal originLongitude;

  @Column(name = "pricing_plan_id", nullable = false)
  @NotNull private UUID pricingPlanId;

  @Column(name = "pricing_plan_version", nullable = false, length = 100)
  @NotNull private String pricingPlanVersion;

  @Column(name = "route_computation_mode", nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  @NotNull private RouteComputationMode routeComputationMode;

  @Column(name = "routing_engine", nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  @NotNull private RoutingEngine routingEngine;

  @Column(name = "status", nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  @NotNull private PricingQuoteStatus status;

  @Column(name = "vehicle_class", nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  @NotNull private VehicleClass vehicleClass;

  @Builder
  public PricingQuote(
      @NotNull BigDecimal baseFareAmount,
      UUID branchId,
      @NotNull String currency,
      @NotNull BigDecimal destinationLatitude,
      @NotNull BigDecimal destinationLongitude,
      @NotNull BigDecimal distanceChargeAmount,
      int distanceMeters,
      int durationSeconds,
      @NotNull String engineCostingUsed,
      OffsetDateTime expiresAt,
      @NotNull BigDecimal finalAmount,
      Boolean isCommitted,
      Boolean isFallback,
      List<PricingQuoteModifier> modifierBreakdown,
      BigDecimal modifiersTotalAmount,
      UUID orderId,
      @NotNull OrderType orderType,
      UUID organizationId,
      @NotNull BigDecimal originLatitude,
      @NotNull BigDecimal originLongitude,
      @NotNull UUID pricingPlanId,
      @NotNull String pricingPlanVersion,
      @NotNull RouteComputationMode routeComputationMode,
      @NotNull RoutingEngine routingEngine,
      PricingQuoteStatus status,
      @NotNull VehicleClass vehicleClass) {

    validateAmount(
        baseFareAmount,
        "Base fare amount is required",
        "Base fare amount must be greater than or equal to 0");
    validateBranchOrganizationConsistency(branchId, organizationId);
    validateCurrency(currency);
    validateAmount(
        distanceChargeAmount,
        "Distance charge amount is required",
        "Distance charge amount must be greater than or equal to 0");
    validateDistanceMeters(distanceMeters);
    validateDurationSeconds(durationSeconds);
    validateEngineCostingUsed(engineCostingUsed);
    validateOrderType(orderType);
    validatePlanReference(pricingPlanId, pricingPlanVersion);
    validateRouteComputationMode(routeComputationMode);
    validateRoutingCoordinates(
        destinationLatitude, destinationLongitude, originLatitude, originLongitude);
    validateRoutingEngine(routingEngine);
    validateVehicleClass(vehicleClass);

    this.baseFareAmount = baseFareAmount;
    this.branchId = branchId;
    this.currency = currency.trim();
    this.destinationLatitude = destinationLatitude;
    this.destinationLongitude = destinationLongitude;
    this.distanceChargeAmount = distanceChargeAmount;
    this.distanceMeters = distanceMeters;
    this.durationSeconds = durationSeconds;
    this.engineCostingUsed = engineCostingUsed.trim();
    this.isCommitted = isCommitted != null && isCommitted;
    this.isFallback = isFallback != null && isFallback;
    this.modifiersTotalAmount =
        modifiersTotalAmount == null ? BigDecimal.ZERO : modifiersTotalAmount;
    this.orderId = orderId;
    this.orderType = orderType;
    this.organizationId = organizationId;
    this.originLatitude = originLatitude;
    this.originLongitude = originLongitude;
    this.pricingPlanId = pricingPlanId;
    this.pricingPlanVersion = pricingPlanVersion.trim();
    this.routeComputationMode = routeComputationMode;
    this.routingEngine = routingEngine;
    this.status = status == null ? PricingQuoteStatus.CREATED : status;
    this.vehicleClass = vehicleClass;

    validateFinalAmount(
        baseFareAmount, distanceChargeAmount, this.modifiersTotalAmount, finalAmount);
    validateCommitExpiryConsistency(this.isCommitted, expiresAt);
    validateFallbackConsistency(this.isFallback, routeComputationMode, routingEngine);

    this.expiresAt = expiresAt;
    this.finalAmount = finalAmount;

    validateStatusConsistency();

    if (modifierBreakdown != null) {
      this.modifierBreakdown.addAll(modifierBreakdown);
    }
  }

  public List<PricingQuoteModifier> getModifierBreakdown() {
    return Collections.unmodifiableList(modifierBreakdown);
  }

  public void apply() {
    if (this.status != PricingQuoteStatus.CREATED) {
      throw new IllegalStateException(
          "Only CREATED pricing quotes can be applied. Current status: " + this.status);
    }
    if (!this.isCommitted) {
      throw new IllegalStateException("Only committed pricing quotes can be applied");
    }
    this.status = PricingQuoteStatus.APPLIED;
  }

  public void expire() {
    if (this.status != PricingQuoteStatus.CREATED) {
      throw new IllegalStateException(
          "Only CREATED pricing quotes can be expired. Current status: " + this.status);
    }
    if (this.isCommitted) {
      throw new IllegalStateException("Committed pricing quotes cannot be expired");
    }
    this.status = PricingQuoteStatus.EXPIRED;
  }

  public void supersede() {
    if (this.status == PricingQuoteStatus.APPLIED) {
      throw new IllegalStateException("Applied pricing quotes cannot be superseded");
    }
    this.status = PricingQuoteStatus.SUPERSEDED;
  }

  private void validateAmount(BigDecimal amount, String requiredMessage, String minimumMessage) {
    if (amount == null) {
      throw new IllegalArgumentException(requiredMessage);
    }
    if (amount.signum() < 0) {
      throw new IllegalArgumentException(minimumMessage);
    }
  }

  private void validateBranchOrganizationConsistency(UUID branchId, UUID organizationId) {
    if (branchId != null && organizationId == null) {
      throw new IllegalArgumentException("Organization id is required when branch id is provided");
    }
  }

  private void validateCommitExpiryConsistency(boolean isCommitted, OffsetDateTime expiresAt) {
    if (isCommitted && expiresAt != null) {
      throw new IllegalArgumentException("Committed pricing quotes cannot have an expiry time");
    }
    if (!isCommitted && expiresAt == null) {
      throw new IllegalArgumentException("Uncommitted pricing quotes must have an expiry time");
    }
  }

  private void validateCurrency(String currency) {
    if (currency == null || currency.isBlank()) {
      throw new IllegalArgumentException("Currency is required");
    }
    if (!MVP_CURRENCY.equals(currency.trim())) {
      throw new IllegalArgumentException("Currency must be KES");
    }
  }

  private void validateDistanceMeters(int distanceMeters) {
    if (distanceMeters < 0) {
      throw new IllegalArgumentException("Distance meters must be greater than or equal to 0");
    }
  }

  private void validateDurationSeconds(int durationSeconds) {
    if (durationSeconds < 0) {
      throw new IllegalArgumentException("Duration seconds must be greater than or equal to 0");
    }
  }

  private void validateEngineCostingUsed(String engineCostingUsed) {
    if (engineCostingUsed == null || engineCostingUsed.isBlank()) {
      throw new IllegalArgumentException("Engine costing used is required");
    }
  }

  private void validateFallbackConsistency(
      boolean isFallback, RouteComputationMode routeComputationMode, RoutingEngine routingEngine) {
    if (isFallback
        && (routeComputationMode != RouteComputationMode.FALLBACK
            || routingEngine != RoutingEngine.HAVERSINE_FALLBACK)) {
      throw new IllegalArgumentException(
          "Fallback quotes must use FALLBACK mode and HAVERSINE_FALLBACK engine");
    }
    if (!isFallback
        && (routeComputationMode != RouteComputationMode.ROUTED
            || routingEngine != RoutingEngine.VALHALLA)) {
      throw new IllegalArgumentException("Routed quotes must use ROUTED mode and VALHALLA engine");
    }
  }

  private void validateFinalAmount(
      BigDecimal baseFareAmount,
      BigDecimal distanceChargeAmount,
      BigDecimal modifiersTotalAmount,
      BigDecimal finalAmount) {
    if (finalAmount == null) {
      throw new IllegalArgumentException("Final amount is required");
    }
    if (finalAmount.signum() < 0) {
      throw new IllegalArgumentException("Final amount must be greater than or equal to 0");
    }
    BigDecimal expected = baseFareAmount.add(distanceChargeAmount).add(modifiersTotalAmount);
    if (expected.compareTo(finalAmount) != 0) {
      throw new IllegalArgumentException(
          "Final amount must equal base fare amount plus distance charge amount plus modifiers"
              + " total amount");
    }
  }

  private void validateOrderType(OrderType orderType) {
    if (orderType == null) {
      throw new IllegalArgumentException("Order type is required");
    }
  }

  private void validatePlanReference(UUID pricingPlanId, String pricingPlanVersion) {
    if (pricingPlanId == null) {
      throw new IllegalArgumentException("Pricing plan id is required");
    }
    if (pricingPlanVersion == null || pricingPlanVersion.isBlank()) {
      throw new IllegalArgumentException("Pricing plan version is required");
    }
  }

  private void validateRouteComputationMode(RouteComputationMode routeComputationMode) {
    if (routeComputationMode == null) {
      throw new IllegalArgumentException("Route computation mode is required");
    }
  }

  private void validateRoutingCoordinates(
      BigDecimal destinationLatitude,
      BigDecimal destinationLongitude,
      BigDecimal originLatitude,
      BigDecimal originLongitude) {
    validateLatitude(destinationLatitude, "Destination latitude is required");
    validateLongitude(destinationLongitude, "Destination longitude is required");
    validateLatitude(originLatitude, "Origin latitude is required");
    validateLongitude(originLongitude, "Origin longitude is required");
  }

  private void validateRoutingEngine(RoutingEngine routingEngine) {
    if (routingEngine == null) {
      throw new IllegalArgumentException("Routing engine is required");
    }
  }

  private void validateStatusConsistency() {
    if (this.status == PricingQuoteStatus.APPLIED && !this.isCommitted) {
      throw new IllegalArgumentException("Applied pricing quotes must be committed");
    }
    if (this.status == PricingQuoteStatus.EXPIRED && this.isCommitted) {
      throw new IllegalArgumentException("Expired pricing quotes cannot be committed");
    }
  }

  private void validateVehicleClass(VehicleClass vehicleClass) {
    if (vehicleClass == null) {
      throw new IllegalArgumentException("Vehicle class is required");
    }
  }

  private void validateLatitude(BigDecimal latitude, String requiredMessage) {
    if (latitude == null) {
      throw new IllegalArgumentException(requiredMessage);
    }
    if (latitude.compareTo(new BigDecimal("-90")) < 0
        || latitude.compareTo(new BigDecimal("90")) > 0) {
      throw new IllegalArgumentException("Latitude must be between -90 and 90");
    }
  }

  private void validateLongitude(BigDecimal longitude, String requiredMessage) {
    if (longitude == null) {
      throw new IllegalArgumentException(requiredMessage);
    }
    if (longitude.compareTo(new BigDecimal("-180")) < 0
        || longitude.compareTo(new BigDecimal("180")) > 0) {
      throw new IllegalArgumentException("Longitude must be between -180 and 180");
    }
  }
}
