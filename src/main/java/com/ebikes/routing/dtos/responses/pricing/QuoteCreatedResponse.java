package com.ebikes.routing.dtos.responses.pricing;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.ebikes.routing.dtos.internal.MoneyDto;

public record QuoteCreatedResponse(
    List<AppliedModifierResponse> appliedModifiers,
    MoneyDto baseFare,
    MoneyDto distanceCharge,
    int distanceMeters,
    int durationSeconds,
    OffsetDateTime expiresAt,
    MoneyDto finalAmount,
    boolean isFallback,
    UUID pricingQuoteId,
    String pricingPlanVersion) {}
