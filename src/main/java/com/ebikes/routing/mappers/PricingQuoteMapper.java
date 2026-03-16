package com.ebikes.routing.mappers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.ebikes.routing.database.entities.PricingQuote;
import com.ebikes.routing.database.entities.PricingQuoteModifier;
import com.ebikes.routing.dtos.internal.LocationDto;
import com.ebikes.routing.dtos.internal.MoneyDto;
import com.ebikes.routing.dtos.responses.pricing.AppliedModifierResponse;
import com.ebikes.routing.dtos.responses.pricing.PricingQuoteDetailResponse;
import com.ebikes.routing.dtos.responses.pricing.PricingQuoteSummaryResponse;
import com.ebikes.routing.dtos.responses.pricing.QuoteCreatedResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PricingQuoteMapper {

  default PricingQuoteDetailResponse toDetailResponse(
      PricingQuote quote, List<AppliedModifierResponse> modifiers) {
    Objects.requireNonNull(quote, "quote must not be null");

    return new PricingQuoteDetailResponse(
        modifiers == null ? List.of() : List.copyOf(modifiers),
        money(quote.getBaseFareAmount(), quote.getCurrency()),
        quote.getBranchId(),
        quote.getCreatedAt(),
        location(quote.getDestinationLatitude(), quote.getDestinationLongitude()),
        money(quote.getDistanceChargeAmount(), quote.getCurrency()),
        quote.getDistanceMeters(),
        quote.getDurationSeconds(),
        quote.getEngineCostingUsed(),
        quote.getExpiresAt(),
        money(quote.getFinalAmount(), quote.getCurrency()),
        quote.getId(),
        quote.isCommitted(),
        quote.isFallback(),
        quote.getOrderId(),
        quote.getOrderType(),
        quote.getOrganizationId(),
        location(quote.getOriginLatitude(), quote.getOriginLongitude()),
        quote.getPricingPlanId(),
        quote.getPricingPlanVersion(),
        quote.getRouteComputationMode(),
        quote.getRoutingEngine(),
        quote.getStatus(),
        quote.getVehicleClass());
  }

  @Mapping(
      target = "finalAmount",
      expression = "java(money(quote.getFinalAmount(), quote.getCurrency()))")
  PricingQuoteSummaryResponse toSummaryResponse(PricingQuote quote);

  default QuoteCreatedResponse toQuoteCreatedResponse(
      PricingQuote quote, List<AppliedModifierResponse> modifiers) {
    Objects.requireNonNull(quote, "quote must not be null");

    return new QuoteCreatedResponse(
        modifiers == null ? List.of() : List.copyOf(modifiers),
        money(quote.getBaseFareAmount(), quote.getCurrency()),
        money(quote.getDistanceChargeAmount(), quote.getCurrency()),
        quote.getDistanceMeters(),
        quote.getDurationSeconds(),
        quote.getExpiresAt(),
        money(quote.getFinalAmount(), quote.getCurrency()),
        quote.isFallback(),
        quote.getId(),
        quote.getPricingPlanVersion());
  }

  @Mapping(
      target = "appliedAmount",
      expression = "java(money(modifier.getAppliedAmount(), modifier.getCurrency()))")
  @Mapping(
      target = "valueAmount",
      expression = "java(money(modifier.getValueAmount(), modifier.getCurrency()))")
  AppliedModifierResponse toAppliedModifierResponse(PricingQuoteModifier modifier);

  default MoneyDto money(BigDecimal amount, String currency) {
    if (amount == null || currency == null) {
      return null;
    }
    return new MoneyDto(amount, currency);
  }

  default LocationDto location(BigDecimal latitude, BigDecimal longitude) {
    if (latitude == null || longitude == null) {
      return null;
    }
    return new LocationDto(latitude, longitude);
  }
}
