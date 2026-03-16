package com.ebikes.routing.dtos.requests.filters;

import com.ebikes.routing.dtos.requests.filters.bases.PricingConfigFilter;
import com.ebikes.routing.enums.ModifierStackingMode;
import com.ebikes.routing.enums.OrderType;
import com.ebikes.routing.enums.PricingModifierType;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PricingModifierFilter extends PricingConfigFilter {

  private OrderType appliesToOrderType;
  private PricingModifierType modifierType;
  private Integer priorityMax;
  private Integer priorityMin;
  private ModifierStackingMode stackingMode;
}
