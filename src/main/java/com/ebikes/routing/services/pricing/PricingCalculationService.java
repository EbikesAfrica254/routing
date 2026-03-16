package com.ebikes.routing.services.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ebikes.routing.database.entities.PricingModifier;
import com.ebikes.routing.database.entities.PricingPlan;
import com.ebikes.routing.dtos.internal.PricingCalculationResult;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PricingCalculationService {

  private static final BigDecimal METERS_PER_KM = new BigDecimal("1000");
  private static final BigDecimal HUNDRED = new BigDecimal("100");
  private static final int SCALE = 4;
  private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

  public PricingCalculationResult calculate(
      PricingPlan plan, List<PricingModifier> modifiers, int distanceMeters) {

    BigDecimal billableKm = new BigDecimal(distanceMeters).divide(METERS_PER_KM, SCALE, ROUNDING);

    BigDecimal subtotal =
        plan.getBaseFareAmount()
            .add(billableKm.multiply(plan.getPerKmRateAmount()).setScale(SCALE, ROUNDING));

    BigDecimal baseFareAmount = plan.getBaseFareAmount();
    BigDecimal distanceChargeAmount =
        billableKm.multiply(plan.getPerKmRateAmount()).setScale(SCALE, ROUNDING);

    List<AppliedModifierSnapshot> applied = new ArrayList<>();

    for (PricingModifier modifier : modifiers) {
      BigDecimal before = subtotal;
      subtotal = applyModifier(subtotal, modifier);
      BigDecimal appliedAmount = subtotal.subtract(before).abs().setScale(SCALE, ROUNDING);

      applied.add(
          new AppliedModifierSnapshot(
              modifier.getId(),
              modifier.getVersion(),
              modifier.getModifierType(),
              modifier.getValueAmount(),
              appliedAmount));

      log.debug(
          "Applied modifier: modifierId={}, type={}, before={}, after={}",
          modifier.getId(),
          modifier.getModifierType(),
          before,
          subtotal);
    }

    subtotal = applyPlanFloorAndCap(subtotal, plan);

    BigDecimal modifiersTotalAmount =
        subtotal.subtract(baseFareAmount).subtract(distanceChargeAmount).setScale(SCALE, ROUNDING);

    log.debug(
        "Pricing calculation complete: baseFare={}, distanceCharge={}, modifiersTotal={}, final={}",
        baseFareAmount,
        distanceChargeAmount,
        modifiersTotalAmount,
        subtotal);

    return new PricingCalculationResult(
        baseFareAmount, distanceChargeAmount, modifiersTotalAmount, subtotal, applied);
  }

  private BigDecimal applyModifier(BigDecimal subtotal, PricingModifier modifier) {
    BigDecimal value = modifier.getValueAmount();

    return switch (modifier.getModifierType()) {
      case FIXED_SURCHARGE, PEAK_WINDOW_SURCHARGE, ORDER_TYPE_SURCHARGE -> subtotal.add(value);
      case FIXED_DISCOUNT, ORDER_TYPE_DISCOUNT -> subtotal.subtract(value).max(BigDecimal.ZERO);
      case PERCENT_SURCHARGE ->
          subtotal.add(subtotal.multiply(value).divide(HUNDRED, SCALE, ROUNDING));
      case PERCENT_DISCOUNT ->
          subtotal
              .subtract(subtotal.multiply(value).divide(HUNDRED, SCALE, ROUNDING))
              .max(BigDecimal.ZERO);
      case MIN_PRICE_FLOOR -> subtotal.max(value);
      case MAX_PRICE_CAP -> subtotal.min(value);
    };
  }

  private BigDecimal applyPlanFloorAndCap(BigDecimal subtotal, PricingPlan plan) {
    subtotal = subtotal.max(plan.getMinimumChargeAmount());

    if (plan.getMaximumChargeAmount() != null) {
      subtotal = subtotal.min(plan.getMaximumChargeAmount());
    }

    return subtotal;
  }
}
