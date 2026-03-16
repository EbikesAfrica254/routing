package com.ebikes.routing.dtos.requests.filters;

import com.ebikes.routing.dtos.requests.filters.bases.PricingConfigFilter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PricingPlanFilter extends PricingConfigFilter {}
