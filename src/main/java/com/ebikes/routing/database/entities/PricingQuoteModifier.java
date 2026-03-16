package com.ebikes.routing.database.entities;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.ebikes.routing.database.entities.bases.BaseEntity;
import com.ebikes.routing.enums.ModifierStackingMode;
import com.ebikes.routing.enums.PricingModifierType;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "pricing_quote_modifiers", schema = "routing")
public class PricingQuoteModifier extends BaseEntity {

  @Column(name = "applied_amount", nullable = false, precision = 19, scale = 4)
  private BigDecimal appliedAmount;

  @Column(name = "condition_snapshot", nullable = false, columnDefinition = "jsonb")
  private String conditionSnapshot;

  @Column(name = "currency", nullable = false, length = 3)
  private String currency;

  @Column(name = "modifier_type", nullable = false, length = 40)
  @Enumerated(EnumType.STRING)
  private PricingModifierType modifierType;

  @Column(name = "modifier_version", nullable = false, length = 100)
  private String modifierVersion;

  @Column(name = "pricing_modifier_id", nullable = false)
  private UUID pricingModifierId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "pricing_quote_id", nullable = false, updatable = false)
  private PricingQuote pricingQuote;

  @Column(name = "priority", nullable = false)
  private int priority;

  @Column(name = "stacking_mode", nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private ModifierStackingMode stackingMode;

  @Column(name = "value_amount", nullable = false, precision = 19, scale = 4)
  private BigDecimal valueAmount;

  @Builder
  public PricingQuoteModifier(
      BigDecimal appliedAmount,
      String conditionSnapshot,
      String currency,
      PricingModifierType modifierType,
      String modifierVersion,
      UUID pricingModifierId,
      PricingQuote pricingQuote,
      int priority,
      ModifierStackingMode stackingMode,
      BigDecimal valueAmount) {
    this.appliedAmount = appliedAmount;
    this.conditionSnapshot = conditionSnapshot;
    this.currency = currency;
    this.modifierType = modifierType;
    this.modifierVersion = modifierVersion;
    this.pricingModifierId = pricingModifierId;
    this.pricingQuote = pricingQuote;
    this.priority = priority;
    this.stackingMode = stackingMode;
    this.valueAmount = valueAmount;
  }
}
