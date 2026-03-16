package com.ebikes.routing.services.pricing;

import java.time.ZoneId;
import java.time.ZonedDateTime;
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
import com.ebikes.routing.database.entities.PricingModifier;
import com.ebikes.routing.database.repositories.PricingModifierRepository;
import com.ebikes.routing.database.specifications.PricingModifierSpecifications;
import com.ebikes.routing.domain.ConditionSet;
import com.ebikes.routing.dtos.events.outgoing.PricingModifierActivatedEvent;
import com.ebikes.routing.dtos.requests.filters.PricingModifierFilter;
import com.ebikes.routing.dtos.requests.pricing.CreatePricingModifierRequest;
import com.ebikes.routing.dtos.requests.pricing.ModifierConditionSetDto;
import com.ebikes.routing.dtos.requests.pricing.UpdatePricingModifierRequest;
import com.ebikes.routing.dtos.responses.api.PaginatedResponse;
import com.ebikes.routing.dtos.responses.pricing.PricingModifierDetailResponse;
import com.ebikes.routing.dtos.responses.pricing.PricingModifierSummaryResponse;
import com.ebikes.routing.enums.ResponseCode;
import com.ebikes.routing.exceptions.DuplicateResourceException;
import com.ebikes.routing.exceptions.ResourceNotFoundException;
import com.ebikes.routing.mappers.PricingModifierMapper;
import com.ebikes.routing.publishers.AuditEventPublisher;
import com.ebikes.routing.services.events.OutboxService;
import com.ebikes.routing.support.audit.AuditMetadataBuilder;
import com.ebikes.routing.support.context.ExecutionContext;
import com.ebikes.routing.support.database.FilterUtilities;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional(readOnly = true)
public class PricingModifierService {

  private final AuditEventPublisher auditEventPublisher;
  private final OutboxService outboxService;
  private final PricingModifierMapper modifierMapper;
  private final PricingModifierRepository modifierRepository;
  private final PricingProperties pricingProperties;

  @Transactional
  public PricingModifierDetailResponse activateModifier(UUID modifierId) {
    log.info("Activating pricing modifier: modifierId={}", modifierId);

    PricingModifier modifier = requireById(modifierId);

    ZonedDateTime now = ZonedDateTime.now(ZoneId.of(pricingProperties.getZoneId()));

    if (modifierRepository.existsActiveConflictForScope(
        modifier.getScopeType(),
        modifier.getScopeId(),
        modifier.getModifierType(),
        modifier.getPriority(),
        now.toOffsetDateTime())) {
      throw new DuplicateResourceException(
          ResponseCode.DUPLICATE_RESOURCE,
          "An active modifier with the same scope, type, and priority already exists:"
              + " scopeType="
              + modifier.getScopeType()
              + ", scopeId="
              + modifier.getScopeId()
              + ", modifierType="
              + modifier.getModifierType()
              + ", priority="
              + modifier.getPriority());
    }

    modifier.activate(ExecutionContext.getUserId(), now.toOffsetDateTime());
    modifier = modifierRepository.save(modifier);

    outboxService.save(
        EventTypes.PricingModifiers.ACTIVATED,
        new PricingModifierActivatedEvent(
            modifier.getActivatedAt(),
            modifier.getId(),
            EventSource.serviceReference(),
            modifier.getScopeId(),
            modifier.getScopeType(),
            modifier.getVersion()),
        RoutingKeys.PRICING_MODIFIER_ACTIVATED);

    auditEventPublisher.publishSuccess(
        modifier.getId(),
        PricingModifier.class.getSimpleName(),
        EventTypes.PricingModifiers.ACTIVATED,
        AuditMetadataBuilder.forPricingModifier(modifier),
        RoutingKeys.PRICING_MODIFIER_AUDIT);

    log.info(
        "Pricing modifier activated: modifierId={}, version={}",
        modifier.getId(),
        modifier.getVersion());

    return modifierMapper.toDetailResponse(modifier);
  }

  @Transactional
  public PricingModifierDetailResponse createModifier(CreatePricingModifierRequest request) {
    log.info(
        "Creating pricing modifier: version={}, scopeType={}, modifierType={}",
        request.version(),
        request.scopeType(),
        request.modifierType());

    PricingModifier modifier =
        PricingModifier.builder()
            .appliesToOrderType(request.appliesToOrderType())
            .conditionSet(toConditionSet(request.conditionSet()))
            .effectiveFrom(request.effectiveFrom())
            .effectiveTo(request.effectiveTo())
            .modifierType(request.modifierType())
            .priority(request.priority())
            .scopeId(request.scopeId())
            .scopeType(request.scopeType())
            .stackingMode(request.stackingMode())
            .valueAmount(request.valueAmount())
            .vehicleClass(request.vehicleClass())
            .version(request.version())
            .build();

    modifier = modifierRepository.save(modifier);

    auditEventPublisher.publishSuccess(
        modifier.getId(),
        PricingModifier.class.getSimpleName(),
        EventTypes.PricingModifiers.CREATED,
        AuditMetadataBuilder.forPricingModifier(modifier),
        RoutingKeys.PRICING_MODIFIER_AUDIT);

    log.info(
        "Pricing modifier created: modifierId={}, version={}",
        modifier.getId(),
        modifier.getVersion());

    return modifierMapper.toDetailResponse(modifier);
  }

  @Transactional
  public PaginatedResponse<PricingModifierSummaryResponse> searchPricingModifiers(
      PricingModifierFilter filter) {
    Pageable pageable =
        FilterUtilities.buildPageable(filter, PricingModifierSpecifications.ALLOWED_SORT_FIELDS);
    Specification<PricingModifier> spec = PricingModifierSpecifications.buildSpecification(filter);
    Page<PricingModifier> page = modifierRepository.findAll(spec, pageable);
    return PaginatedResponse.from(
        "Pricing modifiers retrieved.", page.map(modifierMapper::toSummaryResponse));
  }

  @Transactional
  public PricingModifierDetailResponse updateModifier(
      UUID modifierId, UpdatePricingModifierRequest request) {
    log.info("Updating pricing modifier: modifierId={}", modifierId);

    PricingModifier modifier = requireById(modifierId);

    modifier.updateApplicability(request.appliesToOrderType(), request.vehicleClass());
    modifier.updateConditionSet(toConditionSet(request.conditionSet()));
    modifier.updateEffectiveWindow(request.effectiveFrom(), request.effectiveTo());
    modifier.updatePriority(request.priority());
    modifier.updateValue(request.valueAmount());
    modifier = modifierRepository.save(modifier);

    auditEventPublisher.publishSuccess(
        modifier.getId(),
        PricingModifier.class.getSimpleName(),
        EventTypes.PricingModifiers.CREATED,
        AuditMetadataBuilder.forPricingModifier(modifier),
        RoutingKeys.PRICING_MODIFIER_AUDIT);

    log.info(
        "Pricing modifier updated: modifierId={}, version={}",
        modifier.getId(),
        modifier.getVersion());

    return modifierMapper.toDetailResponse(modifier);
  }

  public PricingModifier requireById(UUID modifierId) {
    return modifierRepository
        .findById(modifierId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    ResponseCode.RESOURCE_NOT_FOUND, "Pricing modifier not found: " + modifierId));
  }

  private ConditionSet toConditionSet(ModifierConditionSetDto dto) {
    if (dto == null) {
      return null;
    }
    return new ConditionSet(
        dto.branchId(),
        dto.daysOfWeek(),
        dto.endHour(),
        dto.organizationId(),
        dto.orderType(),
        dto.startHour(),
        dto.vehicleClass());
  }
}
