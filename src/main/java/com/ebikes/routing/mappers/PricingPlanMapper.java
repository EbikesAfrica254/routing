package com.ebikes.routing.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.ebikes.routing.database.entities.PricingPlan;
import com.ebikes.routing.dtos.responses.pricing.PricingPlanDetailResponse;
import com.ebikes.routing.dtos.responses.pricing.PricingPlanSummaryResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PricingPlanMapper {

  PricingPlanDetailResponse toDetailResponse(PricingPlan plan);

  PricingPlanSummaryResponse toSummaryResponse(PricingPlan plan);
}
