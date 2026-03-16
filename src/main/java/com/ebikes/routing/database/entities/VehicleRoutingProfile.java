package com.ebikes.routing.database.entities;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import com.ebikes.routing.database.entities.bases.AuditableEntity;
import com.ebikes.routing.enums.RoutingEngine;
import com.ebikes.routing.enums.VehicleClass;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "vehicle_routing_profiles", schema = "routing")
public class VehicleRoutingProfile extends AuditableEntity {

  private static final BigDecimal MINIMUM_FALLBACK_MULTIPLIER = new BigDecimal("1.0000");

  @Column(name = "engine_costing_key", nullable = false, length = 50)
  @NotNull private String engineCostingKey;

  @Column(name = "fallback_multiplier", precision = 8, scale = 4)
  private BigDecimal fallbackMultiplier;

  @Column(name = "is_active", nullable = false)
  private boolean isActive;

  @Column(name = "is_fallback_enabled", nullable = false)
  private boolean isFallbackEnabled;

  @Column(name = "routing_engine", nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  @NotNull private RoutingEngine routingEngine;

  @Column(name = "vehicle_class", nullable = false, unique = true, length = 20)
  @Enumerated(EnumType.STRING)
  @NotNull private VehicleClass vehicleClass;

  @Builder
  public VehicleRoutingProfile(
      @NotNull String engineCostingKey,
      BigDecimal fallbackMultiplier,
      Boolean isActive,
      Boolean isFallbackEnabled,
      @NotNull RoutingEngine routingEngine,
      @NotNull VehicleClass vehicleClass) {
    validateEngineCostingKey(engineCostingKey);
    validateRoutingEngine(routingEngine);
    validateVehicleClass(vehicleClass);

    this.engineCostingKey = engineCostingKey.trim();
    this.isActive = isActive == null || isActive;
    this.isFallbackEnabled = isFallbackEnabled == null || isFallbackEnabled;
    this.routingEngine = routingEngine;
    this.vehicleClass = vehicleClass;

    if (this.isFallbackEnabled) {
      validateFallbackMultiplier(fallbackMultiplier);
      this.fallbackMultiplier = fallbackMultiplier;
    } else {
      this.fallbackMultiplier = null;
    }
  }

  public void activate() {
    this.isActive = true;
  }

  public void deactivate() {
    this.isActive = false;
  }

  public void disableFallback() {
    this.fallbackMultiplier = null;
    this.isFallbackEnabled = false;
  }

  public void enableFallback(BigDecimal fallbackMultiplier) {
    validateFallbackMultiplier(fallbackMultiplier);
    this.fallbackMultiplier = fallbackMultiplier;
    this.isFallbackEnabled = true;
  }

  public void updateEngineConfiguration(String engineCostingKey, RoutingEngine routingEngine) {
    validateEngineCostingKey(engineCostingKey);
    validateRoutingEngine(routingEngine);

    this.engineCostingKey = engineCostingKey.trim();
    this.routingEngine = routingEngine;
  }

  public void updateFallbackMultiplier(BigDecimal fallbackMultiplier) {
    if (!this.isFallbackEnabled) {
      throw new IllegalStateException(
          "Fallback multiplier cannot be updated while fallback is disabled");
    }

    validateFallbackMultiplier(fallbackMultiplier);
    this.fallbackMultiplier = fallbackMultiplier;
  }

  private void validateEngineCostingKey(String engineCostingKey) {
    if (engineCostingKey == null || engineCostingKey.isBlank()) {
      throw new IllegalArgumentException("Engine costing key is required");
    }

    if (!"auto".equals(engineCostingKey) && !"bicycle".equals(engineCostingKey)) {
      throw new IllegalArgumentException("Engine costing key must be one of: bicycle, auto");
    }
  }

  private void validateFallbackMultiplier(BigDecimal fallbackMultiplier) {
    if (fallbackMultiplier == null) {
      throw new IllegalArgumentException(
          "Fallback multiplier is required when fallback is enabled");
    }

    if (fallbackMultiplier.compareTo(MINIMUM_FALLBACK_MULTIPLIER) < 0) {
      throw new IllegalArgumentException(
          "Fallback multiplier must be greater than or equal to 1.0000");
    }
  }

  private void validateRoutingEngine(RoutingEngine routingEngine) {
    if (routingEngine == null) {
      throw new IllegalArgumentException("Routing engine is required");
    }
  }

  private void validateVehicleClass(VehicleClass vehicleClass) {
    if (vehicleClass == null) {
      throw new IllegalArgumentException("Vehicle class is required");
    }
  }
}
