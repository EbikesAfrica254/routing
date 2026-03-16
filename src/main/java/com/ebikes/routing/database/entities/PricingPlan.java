package com.ebikes.routing.database.entities;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import com.ebikes.routing.database.entities.bases.ScopedLifecycleEntity;
import com.ebikes.routing.enums.PricingStatus;
import com.ebikes.routing.enums.ScopeType;
import com.ebikes.routing.enums.VehicleClass;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "pricing_plans", schema = "routing")
public class PricingPlan extends ScopedLifecycleEntity {

  private static final String ENTITY_NAME = "pricing plans";

  @Column(name = "base_fare_amount", nullable = false, precision = 19, scale = 4)
  @NotNull private BigDecimal baseFareAmount;

  @Column(name = "effective_from", nullable = false, columnDefinition = "TIMESTAMPTZ")
  @NotNull private OffsetDateTime effectiveFrom;

  @Column(name = "effective_to", columnDefinition = "TIMESTAMPTZ")
  private OffsetDateTime effectiveTo;

  @Column(name = "maximum_charge_amount", precision = 19, scale = 4)
  private BigDecimal maximumChargeAmount;

  @Column(name = "minimum_charge_amount", nullable = false, precision = 19, scale = 4)
  @NotNull private BigDecimal minimumChargeAmount;

  @Column(name = "per_km_rate_amount", nullable = false, precision = 19, scale = 4)
  @NotNull private BigDecimal perKmRateAmount;

  @Column(name = "vehicle_class", nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  @NotNull private VehicleClass vehicleClass;

  @Column(name = "version", nullable = false, unique = true, length = 100)
  @NotNull private String version;

  @Builder
  public PricingPlan(
      OffsetDateTime activatedAt,
      String activatedBy,
      OffsetDateTime archivedAt,
      String archivedBy,
      @NotNull BigDecimal baseFareAmount,
      @NotNull OffsetDateTime effectiveFrom,
      OffsetDateTime effectiveTo,
      BigDecimal maximumChargeAmount,
      @NotNull BigDecimal minimumChargeAmount,
      @NotNull BigDecimal perKmRateAmount,
      UUID scopeId,
      @NotNull ScopeType scopeType,
      PricingStatus status,
      @NotNull VehicleClass vehicleClass,
      @NotNull String version) {
    validateBaseFareAmount(baseFareAmount);
    validateEffectiveWindow(effectiveFrom, effectiveTo);
    validateMaximumChargeAmount(maximumChargeAmount, minimumChargeAmount);
    validateMinimumChargeAmount(minimumChargeAmount);
    validatePerKmRateAmount(perKmRateAmount);
    validateVehicleClass(vehicleClass);
    validateVersion(version);

    this.baseFareAmount = baseFareAmount;
    this.effectiveFrom = effectiveFrom;
    this.effectiveTo = effectiveTo;
    this.maximumChargeAmount = maximumChargeAmount;
    this.minimumChargeAmount = minimumChargeAmount;
    this.perKmRateAmount = perKmRateAmount;
    this.vehicleClass = vehicleClass;
    this.version = version.trim();

    initializeScopedLifecycle(
        scopeId, scopeType, status, activatedAt, activatedBy, archivedAt, archivedBy);
  }

  public void activate(String activatedBy, OffsetDateTime activatedAt) {
    activateLifecycle(activatedBy, activatedAt, ENTITY_NAME);
  }

  public void archive(String archivedBy, OffsetDateTime archivedAt) {
    archiveLifecycle(archivedBy, archivedAt, ENTITY_NAME);
  }

  public void updateAmounts(
      BigDecimal baseFareAmount,
      BigDecimal maximumChargeAmount,
      BigDecimal minimumChargeAmount,
      BigDecimal perKmRateAmount) {
    ensureDraft(ENTITY_NAME);

    validateBaseFareAmount(baseFareAmount);
    validateMaximumChargeAmount(maximumChargeAmount, minimumChargeAmount);
    validateMinimumChargeAmount(minimumChargeAmount);
    validatePerKmRateAmount(perKmRateAmount);

    this.baseFareAmount = baseFareAmount;
    this.maximumChargeAmount = maximumChargeAmount;
    this.minimumChargeAmount = minimumChargeAmount;
    this.perKmRateAmount = perKmRateAmount;
  }

  public void updateEffectiveWindow(OffsetDateTime effectiveFrom, OffsetDateTime effectiveTo) {
    ensureDraft(ENTITY_NAME);
    validateEffectiveWindow(effectiveFrom, effectiveTo);

    this.effectiveFrom = effectiveFrom;
    this.effectiveTo = effectiveTo;
  }

  @Override
  public void updateScope(UUID scopeId, ScopeType scopeType) {
    ensureDraft(ENTITY_NAME);
    super.updateScope(scopeId, scopeType);
  }

  private void validateBaseFareAmount(BigDecimal baseFareAmount) {
    if (baseFareAmount == null) {
      throw new IllegalArgumentException("Base fare amount is required");
    }
    if (baseFareAmount.signum() < 0) {
      throw new IllegalArgumentException("Base fare amount must be greater than or equal to 0");
    }
  }

  private void validateEffectiveWindow(OffsetDateTime effectiveFrom, OffsetDateTime effectiveTo) {
    if (effectiveFrom == null) {
      throw new IllegalArgumentException("Effective from is required");
    }
    if (effectiveTo != null && !effectiveTo.isAfter(effectiveFrom)) {
      throw new IllegalArgumentException("Effective to must be greater than effective from");
    }
  }

  private void validateMaximumChargeAmount(
      BigDecimal maximumChargeAmount, BigDecimal minimumChargeAmount) {
    if (maximumChargeAmount != null && maximumChargeAmount.signum() < 0) {
      throw new IllegalArgumentException(
          "Maximum charge amount must be greater than or equal to 0");
    }

    if (maximumChargeAmount != null
        && minimumChargeAmount != null
        && maximumChargeAmount.compareTo(minimumChargeAmount) < 0) {
      throw new IllegalArgumentException(
          "Maximum charge amount must be greater than or equal to minimum charge amount");
    }
  }

  private void validateMinimumChargeAmount(BigDecimal minimumChargeAmount) {
    if (minimumChargeAmount == null) {
      throw new IllegalArgumentException("Minimum charge amount is required");
    }
    if (minimumChargeAmount.signum() < 0) {
      throw new IllegalArgumentException(
          "Minimum charge amount must be greater than or equal to 0");
    }
  }

  private void validatePerKmRateAmount(BigDecimal perKmRateAmount) {
    if (perKmRateAmount == null) {
      throw new IllegalArgumentException("Per km rate amount is required");
    }
    if (perKmRateAmount.signum() < 0) {
      throw new IllegalArgumentException("Per km rate amount must be greater than or equal to 0");
    }
  }

  private void validateVehicleClass(VehicleClass vehicleClass) {
    if (vehicleClass == null) {
      throw new IllegalArgumentException("Vehicle class is required");
    }
  }

  private void validateVersion(String version) {
    if (version == null || version.isBlank()) {
      throw new IllegalArgumentException("Version is required");
    }
  }
}
