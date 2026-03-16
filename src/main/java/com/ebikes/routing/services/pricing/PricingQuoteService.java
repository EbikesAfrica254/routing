package com.ebikes.routing.services.pricing;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ebikes.routing.configurations.properties.PricingProperties;
import com.ebikes.routing.constants.EventConstants.EventSource;
import com.ebikes.routing.constants.EventConstants.EventTypes;
import com.ebikes.routing.constants.EventConstants.RoutingKeys;
import com.ebikes.routing.database.converters.ConditionSetConverter;
import com.ebikes.routing.database.entities.PricingModifier;
import com.ebikes.routing.database.entities.PricingPlan;
import com.ebikes.routing.database.entities.PricingQuote;
import com.ebikes.routing.database.entities.PricingQuoteModifier;
import com.ebikes.routing.database.repositories.PricingModifierRepository;
import com.ebikes.routing.database.repositories.PricingPlanRepository;
import com.ebikes.routing.database.repositories.PricingQuoteModifierRepository;
import com.ebikes.routing.database.repositories.PricingQuoteRepository;
import com.ebikes.routing.database.repositories.VehicleRoutingProfileRepository;
import com.ebikes.routing.database.specifications.PricingQuoteSpecifications;
import com.ebikes.routing.domain.ModifierEvaluationContext;
import com.ebikes.routing.dtos.events.outgoing.PricingQuoteCreatedEvent;
import com.ebikes.routing.dtos.internal.PricingCalculationResult;
import com.ebikes.routing.dtos.internal.RouteResult;
import com.ebikes.routing.dtos.requests.filters.PricingQuoteFilter;
import com.ebikes.routing.dtos.requests.routing.CreateQuoteRequest;
import com.ebikes.routing.dtos.responses.api.PaginatedResponse;
import com.ebikes.routing.dtos.responses.pricing.AppliedModifierResponse;
import com.ebikes.routing.dtos.responses.pricing.PricingQuoteDetailResponse;
import com.ebikes.routing.dtos.responses.pricing.PricingQuoteSummaryResponse;
import com.ebikes.routing.dtos.responses.pricing.QuoteCreatedResponse;
import com.ebikes.routing.enums.ResponseCode;
import com.ebikes.routing.exceptions.ResourceNotFoundException;
import com.ebikes.routing.exceptions.ValidationException;
import com.ebikes.routing.mappers.PricingQuoteMapper;
import com.ebikes.routing.publishers.AuditEventPublisher;
import com.ebikes.routing.services.events.OutboxService;
import com.ebikes.routing.services.routing.RoutingService;
import com.ebikes.routing.support.audit.AuditMetadataBuilder;
import com.ebikes.routing.support.database.FilterUtilities;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional(readOnly = true)
public class PricingQuoteService {

  private static final String MVP_CURRENCY = "KES";
  private static final BigDecimal COORDINATE_EPSILON = new BigDecimal("0.0001");
  private static final ConditionSetConverter CONDITION_SET_CONVERTER = new ConditionSetConverter();

  private final AuditEventPublisher auditEventPublisher;
  private final ModifierEvaluationService modifierEvaluationService;
  private final OutboxService outboxService;
  private final PricingCalculationService calculationService;
  private final PricingModifierRepository modifierRepository;
  private final PricingPlanRepository planRepository;
  private final PricingProperties pricingProperties;
  private final PricingQuoteMapper quoteMapper;
  private final PricingQuoteModifierRepository quoteModifierRepository;
  private final PricingQuoteRepository quoteRepository;
  private final RoutingService routingService;
  private final VehicleRoutingProfileRepository vehicleRoutingProfileRepository;

  @Transactional
  public QuoteCreatedResponse createQuote(CreateQuoteRequest request) {
    log.info(
        "Creating pricing quote: vehicleClass={}, orderType={}, commitQuote={}",
        request.vehicleClass(),
        request.orderType(),
        request.commitQuote());

    validateQuoteRequest(request);

    var profile =
        vehicleRoutingProfileRepository
            .findByVehicleClass(request.vehicleClass())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        ResponseCode.RESOURCE_NOT_FOUND,
                        "No routing profile found for vehicleClass=" + request.vehicleClass()));

    RouteResult route = routingService.route(request.origin(), request.destination(), profile);

    ZonedDateTime now = ZonedDateTime.now(ZoneId.of(pricingProperties.getZoneId()));

    PricingPlan plan =
        planRepository
            .findActivePlanForScope(
                request.vehicleClass().name(),
                request.branchId(),
                request.organizationId(),
                now.toOffsetDateTime())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        ResponseCode.RESOURCE_NOT_FOUND,
                        "No active pricing plan found for vehicleClass="
                            + request.vehicleClass()
                            + ", organizationId="
                            + request.organizationId()
                            + ", branchId="
                            + request.branchId()));

    List<PricingModifier> candidates =
        modifierRepository.findActiveModifiersForScopes(
            request.vehicleClass(),
            request.orderType(),
            request.organizationId(),
            request.branchId(),
            now.toOffsetDateTime());

    ModifierEvaluationContext context =
        new ModifierEvaluationContext(
            request.branchId(),
            request.organizationId(),
            request.orderType(),
            request.vehicleClass(),
            now,
            ZoneId.of(pricingProperties.getZoneId()));

    List<PricingModifier> applicable = modifierEvaluationService.evaluate(candidates, context);

    PricingCalculationResult calculation =
        calculationService.calculate(plan, applicable, route.distanceMeters());

    OffsetDateTime expiresAt =
        request.commitQuote()
            ? null
            : now.toOffsetDateTime().plusMinutes(pricingProperties.getPreviewExpiryMinutes());

    PricingQuote quote =
        PricingQuote.builder()
            .baseFareAmount(calculation.baseFareAmount())
            .branchId(request.branchId())
            .currency(MVP_CURRENCY)
            .destinationLatitude(request.destination().latitude())
            .destinationLongitude(request.destination().longitude())
            .distanceChargeAmount(calculation.distanceChargeAmount())
            .distanceMeters(route.distanceMeters())
            .durationSeconds(route.durationSeconds())
            .engineCostingUsed(route.engineCostingUsed())
            .expiresAt(expiresAt)
            .finalAmount(calculation.finalAmount())
            .isCommitted(request.commitQuote())
            .isFallback(route.isFallback())
            .modifiersTotalAmount(calculation.modifiersTotalAmount())
            .orderId(request.orderId())
            .orderType(request.orderType())
            .organizationId(request.organizationId())
            .originLatitude(request.origin().latitude())
            .originLongitude(request.origin().longitude())
            .pricingPlanId(plan.getId())
            .pricingPlanVersion(plan.getVersion())
            .routeComputationMode(route.routeComputationMode())
            .routingEngine(route.routingEngine())
            .vehicleClass(request.vehicleClass())
            .build();

    quote = quoteRepository.save(quote);

    List<PricingQuoteModifier> quoteModifiers = buildQuoteModifiers(quote, applicable, calculation);

    if (!quoteModifiers.isEmpty()) {
      quoteModifierRepository.saveAll(quoteModifiers);
    }

    if (request.commitQuote()) {
      outboxService.save(
          EventTypes.PricingQuotes.CREATED,
          new PricingQuoteCreatedEvent(
              quote.getBranchId(),
              quote.isFallback(),
              quote.getFinalAmount(),
              MVP_CURRENCY,
              quote.getOrderType(),
              quote.getOrderId(),
              quote.getOrganizationId(),
              quote.getId(),
              quote.getPricingPlanVersion(),
              quote.getCreatedAt(),
              EventSource.serviceReference(),
              quote.getVehicleClass()),
          RoutingKeys.PRICING_QUOTE_CREATED);
    }

    auditEventPublisher.publishSuccess(
        quote.getId(),
        PricingQuote.class.getSimpleName(),
        EventTypes.PricingQuotes.CREATED,
        AuditMetadataBuilder.forPricingQuote(quote),
        RoutingKeys.PRICING_QUOTE_AUDIT);

    log.info(
        "Pricing quote created: quoteId={}, commitQuote={}, isFallback={}",
        quote.getId(),
        request.commitQuote(),
        route.isFallback());

    List<AppliedModifierResponse> appliedModifierResponses =
        quoteModifiers.stream().map(quoteMapper::toAppliedModifierResponse).toList();

    return quoteMapper.toQuoteCreatedResponse(quote, appliedModifierResponses);
  }

  public PricingQuoteDetailResponse getQuote(UUID quoteId) {
    log.info("Fetching pricing quote: quoteId={}", quoteId);

    PricingQuote quote = requireById(quoteId);

    List<PricingQuoteModifier> quoteModifiers =
        quoteModifierRepository.findByPricingQuoteId(quoteId);

    List<AppliedModifierResponse> appliedModifierResponses =
        quoteModifiers.stream().map(quoteMapper::toAppliedModifierResponse).toList();

    return quoteMapper.toDetailResponse(quote, appliedModifierResponses);
  }

  public PricingQuote requireById(UUID quoteId) {
    return quoteRepository
        .findById(quoteId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    ResponseCode.RESOURCE_NOT_FOUND, "Pricing quote not found: " + quoteId));
  }

  public PaginatedResponse<PricingQuoteSummaryResponse> searchQuotes(PricingQuoteFilter filter) {
    Pageable pageable =
        FilterUtilities.buildPageable(filter, PricingQuoteSpecifications.ALLOWED_SORT_FIELDS);
    Specification<PricingQuote> spec = PricingQuoteSpecifications.buildSpecification(filter);
    Page<PricingQuote> page = quoteRepository.findAll(spec, pageable);
    return PaginatedResponse.from(null, page.map(quoteMapper::toSummaryResponse));
  }

  private void validateQuoteRequest(CreateQuoteRequest request) {
    if (request.branchId() != null && request.organizationId() == null) {
      throw new ValidationException(
          ResponseCode.INVALID_ARGUMENTS,
          "organizationId is required when branchId is provided",
          "organizationId",
          null);
    }

    if (request.commitQuote() && request.orderId() == null) {
      throw new ValidationException(
          ResponseCode.INVALID_ARGUMENTS,
          "orderId is required when commitQuote is true",
          "orderId",
          null);
    }

    if (isEffectivelyIdentical(request)) {
      throw new ValidationException(
          ResponseCode.INVALID_ARGUMENTS,
          "Origin and destination coordinates are effectively identical",
          "destination",
          null);
    }
  }

  private boolean isEffectivelyIdentical(CreateQuoteRequest request) {
    return request
                .origin()
                .latitude()
                .subtract(request.destination().latitude())
                .abs()
                .compareTo(COORDINATE_EPSILON)
            < 0
        && request
                .origin()
                .longitude()
                .subtract(request.destination().longitude())
                .abs()
                .compareTo(COORDINATE_EPSILON)
            < 0;
  }

  private List<PricingQuoteModifier> buildQuoteModifiers(
      PricingQuote quote, List<PricingModifier> applicable, PricingCalculationResult calculation) {

    return calculation.appliedModifiers().stream()
        .map(
            snapshot -> {
              PricingModifier source =
                  applicable.stream()
                      .filter(m -> m.getId().equals(snapshot.modifierId()))
                      .findFirst()
                      .orElseThrow();

              return PricingQuoteModifier.builder()
                  .appliedAmount(snapshot.appliedAmount())
                  .conditionSnapshot(
                      CONDITION_SET_CONVERTER.convertToDatabaseColumn(source.getConditionSet()))
                  .currency(MVP_CURRENCY)
                  .modifierType(snapshot.modifierType())
                  .modifierVersion(snapshot.modifierVersion())
                  .pricingModifierId(snapshot.modifierId())
                  .pricingQuote(quote)
                  .priority(source.getPriority())
                  .stackingMode(source.getStackingMode())
                  .valueAmount(snapshot.valueAmount())
                  .build();
            })
        .toList();
  }
}
