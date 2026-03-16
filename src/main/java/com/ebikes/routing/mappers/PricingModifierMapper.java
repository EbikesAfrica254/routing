package com.ebikes.routing.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.ebikes.routing.database.entities.PricingModifier;
import com.ebikes.routing.domain.ConditionSet;
import com.ebikes.routing.dtos.requests.pricing.ModifierConditionSetDto;
import com.ebikes.routing.dtos.responses.pricing.PricingModifierDetailResponse;
import com.ebikes.routing.dtos.responses.pricing.PricingModifierSummaryResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PricingModifierMapper {

  PricingModifierDetailResponse toDetailResponse(PricingModifier modifier);

  PricingModifierSummaryResponse toSummaryResponse(PricingModifier modifier);

  ModifierConditionSetDto toConditionSetDto(ConditionSet conditionSet);
}
