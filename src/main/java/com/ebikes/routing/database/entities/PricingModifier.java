package com.ebikes.routing.database.entities;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import com.ebikes.routing.database.converters.ConditionSetConverter;
import com.ebikes.routing.database.entities.bases.ScopedLifecycleEntity;
import com.ebikes.routing.domain.ConditionSet;
import com.ebikes.routing.enums.ModifierStackingMode;
import com.ebikes.routing.enums.OrderType;
import com.ebikes.routing.enums.PricingModifierType;
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
@Table(name = "pricing_modifiers", schema = "routing")
public final class PricingModifier extends ScopedLifecycleEntity {

  @Serial private static final long serialVersionUID = 1L;

  private static final String ENTITY_NAME = "pricing modifiers";

  @Column(name = "applies_to_order_type", length = 20)
  @Enumerated(EnumType.STRING)
  private OrderType appliesToOrderType;

  @Column(name = "condition_set", nullable = false, columnDefinition = "jsonb")
  @Convert(converter = ConditionSetConverter.class)
  private ConditionSet conditionSet;

  @Column(name = "effective_from", nullable = false, columnDefinition = "TIMESTAMPTZ")
  @NotNull private OffsetDateTime effectiveFrom;

  @Column(name = "effective_to", columnDefinition = "TIMESTAMPTZ")
  private OffsetDateTime effectiveTo;

  @Column(name = "modifier_type", nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  @NotNull private PricingModifierType modifierType;

  @Column(name = "priority", nullable = false)
  private int priority;

  @Column(name = "stacking_mode", nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  @NotNull private ModifierStackingMode stackingMode;

  @Column(name = "value_amount", nullable = false, precision = 19, scale = 4)
  @NotNull private BigDecimal valueAmount;

  @Column(name = "vehicle_class", length = 20)
  @Enumerated(EnumType.STRING)
  private VehicleClass vehicleClass;

  @Column(name = "version", nullable = false, unique = true, length = 100)
  @NotNull private String version;

  @Builder
  public PricingModifier(
      OffsetDateTime activatedAt,
      String activatedBy,
      OffsetDateTime archivedAt,
      String archivedBy,
      OrderType appliesToOrderType,
      @NotNull ConditionSet conditionSet,
      @NotNull OffsetDateTime effectiveFrom,
      OffsetDateTime effectiveTo,
      @NotNull PricingModifierType modifierType,
      int priority,
      UUID scopeId,
      @NotNull ScopeType scopeType,
      @NotNull ModifierStackingMode stackingMode,
      PricingStatus status,
      @NotNull BigDecimal valueAmount,
      VehicleClass vehicleClass,
      @NotNull String version) {
    validateConditionSet(conditionSet);
    validateEffectiveWindow(effectiveFrom, effectiveTo);
    validateModifierType(modifierType);
    validatePriority(priority);
    validateStackingMode(stackingMode);
    validateValueAmount(valueAmount);
    validateVersion(version);

    this.appliesToOrderType = appliesToOrderType;
    this.conditionSet = conditionSet;
    this.effectiveFrom = effectiveFrom;
    this.effectiveTo = effectiveTo;
    this.modifierType = modifierType;
    this.priority = priority;
    this.stackingMode = stackingMode;
    this.valueAmount = valueAmount;
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

  public void updateApplicability(OrderType orderType, VehicleClass vehicleClass) {
    ensureDraft(ENTITY_NAME);
    this.appliesToOrderType = orderType;
    this.vehicleClass = vehicleClass;
  }

  public void updateConditionSet(ConditionSet conditionSet) {
    ensureDraft(ENTITY_NAME);
    validateConditionSet(conditionSet);
    this.conditionSet = conditionSet;
  }

  public void updateEffectiveWindow(OffsetDateTime effectiveFrom, OffsetDateTime effectiveTo) {
    ensureDraft(ENTITY_NAME);
    validateEffectiveWindow(effectiveFrom, effectiveTo);
    this.effectiveFrom = effectiveFrom;
    this.effectiveTo = effectiveTo;
  }

  public void updatePriority(int priority) {
    ensureDraft(ENTITY_NAME);
    validatePriority(priority);
    this.priority = priority;
  }

  @Override
  public void updateScope(UUID scopeId, ScopeType scopeType) {
    ensureDraft(ENTITY_NAME);
    super.updateScope(scopeId, scopeType);
  }

  public void updateValue(BigDecimal valueAmount) {
    ensureDraft(ENTITY_NAME);
    validateValueAmount(valueAmount);
    this.valueAmount = valueAmount;
  }

  public void updateVersion(String version) {
    ensureDraft(ENTITY_NAME);
    validateVersion(version);
    this.version = version.trim();
  }

  private void validateConditionSet(ConditionSet conditionSet) {
    if (conditionSet == null) {
      throw new IllegalArgumentException("Condition set is required");
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

  private void validateModifierType(PricingModifierType modifierType) {
    if (modifierType == null) {
      throw new IllegalArgumentException("Modifier type is required");
    }
  }

  private void validatePriority(int priority) {
    if (priority < 0) {
      throw new IllegalArgumentException("Priority must be greater than or equal to 0");
    }
  }

  private void validateStackingMode(ModifierStackingMode stackingMode) {
    if (stackingMode == null) {
      throw new IllegalArgumentException("Stacking mode is required");
    }
  }

  private void validateValueAmount(BigDecimal valueAmount) {
    if (valueAmount == null) {
      throw new IllegalArgumentException("Modifier value is required");
    }

    if (valueAmount.signum() < 0) {
      throw new IllegalArgumentException("Modifier value must be greater than or equal to 0");
    }
  }

  private void validateVersion(String version) {
    if (version == null || version.isBlank()) {
      throw new IllegalArgumentException("Version is required");
    }
  }
}
