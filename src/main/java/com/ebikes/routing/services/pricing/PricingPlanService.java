package com.ebikes.routing.services.pricing;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ebikes.routing.configurations.properties.PricingProperties;
import com.ebikes.routing.constants.EventConstants.EventSource;
import com.ebikes.routing.constants.EventConstants.EventTypes;
import com.ebikes.routing.constants.EventConstants.RoutingKeys;
import com.ebikes.routing.database.entities.PricingModifier;
import com.ebikes.routing.database.entities.PricingPlan;
import com.ebikes.routing.database.repositories.PricingModifierRepository;
import com.ebikes.routing.database.repositories.PricingPlanRepository;
import com.ebikes.routing.domain.ModifierEvaluationContext;
import com.ebikes.routing.dtos.events.outgoing.PricingPlanActivatedEvent;
import com.ebikes.routing.dtos.requests.pricing.CreatePricingPlanRequest;
import com.ebikes.routing.dtos.requests.pricing.UpdatePricingPlanRequest;
import com.ebikes.routing.dtos.responses.pricing.PricingPlanDetailResponse;
import com.ebikes.routing.dtos.responses.pricing.ResolvedPricingResponse;
import com.ebikes.routing.enums.OrderType;
import com.ebikes.routing.enums.ResponseCode;
import com.ebikes.routing.enums.VehicleClass;
import com.ebikes.routing.exceptions.DuplicateResourceException;
import com.ebikes.routing.exceptions.ResourceNotFoundException;
import com.ebikes.routing.mappers.PricingModifierMapper;
import com.ebikes.routing.mappers.PricingPlanMapper;
import com.ebikes.routing.publishers.AuditEventPublisher;
import com.ebikes.routing.services.events.OutboxService;
import com.ebikes.routing.support.audit.AuditMetadataBuilder;
import com.ebikes.routing.support.context.ExecutionContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional(readOnly = true)
public class PricingPlanService {

  private final AuditEventPublisher auditEventPublisher;
  private final ModifierEvaluationService modifierEvaluationService;
  private final OutboxService outboxService;
  private final PricingModifierRepository modifierRepository;
  private final PricingModifierMapper modifierMapper;
  private final PricingPlanMapper planMapper;
  private final PricingPlanRepository planRepository;
  private final PricingProperties pricingProperties;

  @Transactional
  public PricingPlanDetailResponse createPlan(CreatePricingPlanRequest request) {
    log.info(
        "Creating pricing plan: version={}, scopeType={}, vehicleClass={}",
        request.version(),
        request.scopeType(),
        request.vehicleClass());

    PricingPlan plan =
        PricingPlan.builder()
            .baseFareAmount(request.baseFareAmount())
            .effectiveFrom(request.effectiveFrom())
            .effectiveTo(request.effectiveTo())
            .maximumChargeAmount(request.maximumChargeAmount())
            .minimumChargeAmount(request.minimumChargeAmount())
            .perKmRateAmount(request.perKmRateAmount())
            .scopeId(request.scopeId())
            .scopeType(request.scopeType())
            .vehicleClass(request.vehicleClass())
            .version(request.version())
            .build();

    plan = planRepository.save(plan);

    auditEventPublisher.publishSuccess(
        plan.getId(),
        PricingPlan.class.getSimpleName(),
        EventTypes.PricingPlans.CREATED,
        AuditMetadataBuilder.forPricingPlan(plan),
        RoutingKeys.PRICING_PLAN_AUDIT);

    log.info("Pricing plan created: planId={}, version={}", plan.getId(), plan.getVersion());

    return planMapper.toDetailResponse(plan);
  }

  @Transactional
  public PricingPlanDetailResponse activatePlan(UUID planId) {
    log.info("Activating pricing plan: planId={}", planId);

    PricingPlan plan = requireById(planId);

    if (planRepository.existsActiveForScopeAndVehicleClass(
        plan.getScopeType(), plan.getScopeId(), plan.getVehicleClass())) {
      throw new DuplicateResourceException(
          ResponseCode.DUPLICATE_RESOURCE,
          "An active pricing plan already exists for scope "
              + plan.getScopeType()
              + " / "
              + plan.getScopeId()
              + " / "
              + plan.getVehicleClass());
    }

    ZonedDateTime now = ZonedDateTime.now(ZoneId.of(pricingProperties.getZoneId()));
    plan.activate(ExecutionContext.getUserId(), now.toOffsetDateTime());
    plan = planRepository.save(plan);

    outboxService.save(
        EventTypes.PricingPlans.ACTIVATED,
        new PricingPlanActivatedEvent(
            plan.getActivatedAt(),
            plan.getEffectiveFrom(),
            plan.getId(),
            EventSource.serviceReference(),
            plan.getScopeId(),
            plan.getScopeType(),
            plan.getVersion(),
            plan.getVehicleClass()),
        RoutingKeys.PRICING_PLAN_ACTIVATED);

    auditEventPublisher.publishSuccess(
        plan.getId(),
        PricingPlan.class.getSimpleName(),
        EventTypes.PricingPlans.ACTIVATED,
        AuditMetadataBuilder.forPricingPlan(plan),
        RoutingKeys.PRICING_PLAN_AUDIT);

    log.info("Pricing plan activated: planId={}, version={}", plan.getId(), plan.getVersion());

    return planMapper.toDetailResponse(plan);
  }

  @Transactional
  public PricingPlanDetailResponse updatePlan(UUID planId, UpdatePricingPlanRequest request) {
    log.info("Updating pricing plan: planId={}", planId);

    PricingPlan plan = requireById(planId);

    plan.updateAmounts(
        request.baseFareAmount(),
        request.maximumChargeAmount(),
        request.minimumChargeAmount(),
        request.perKmRateAmount());
    plan.updateEffectiveWindow(request.effectiveFrom(), request.effectiveTo());
    plan = planRepository.save(plan);

    auditEventPublisher.publishSuccess(
        plan.getId(),
        PricingPlan.class.getSimpleName(),
        EventTypes.PricingPlans.CREATED,
        AuditMetadataBuilder.forPricingPlan(plan),
        RoutingKeys.PRICING_PLAN_AUDIT);

    log.info("Pricing plan updated: planId={}, version={}", plan.getId(), plan.getVersion());

    return planMapper.toDetailResponse(plan);
  }

  public ResolvedPricingResponse resolvePlan(
      VehicleClass vehicleClass, UUID organizationId, UUID branchId, OrderType orderType) {
    log.info(
        "Resolving pricing plan: vehicleClass={}, organizationId={}, branchId={}, orderType={}",
        vehicleClass,
        organizationId,
        branchId,
        orderType);

    ZonedDateTime now = ZonedDateTime.now(ZoneId.of(pricingProperties.getZoneId()));

    PricingPlan plan =
        planRepository
            .findActivePlanForScope(
                vehicleClass.name(), branchId, organizationId, now.toOffsetDateTime())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        ResponseCode.RESOURCE_NOT_FOUND,
                        "No active pricing plan found for vehicleClass="
                            + vehicleClass
                            + ", organizationId="
                            + organizationId
                            + ", branchId="
                            + branchId));

    List<PricingModifier> candidates =
        modifierRepository.findActiveModifiersForScopes(
            vehicleClass, orderType, organizationId, branchId, now.toOffsetDateTime());

    ModifierEvaluationContext context =
        new ModifierEvaluationContext(
            branchId,
            organizationId,
            orderType,
            vehicleClass,
            now,
            ZoneId.of(pricingProperties.getZoneId()));

    List<PricingModifier> applicable = modifierEvaluationService.evaluate(candidates, context);

    return new ResolvedPricingResponse(
        applicable.stream().map(modifierMapper::toSummaryResponse).toList(),
        plan.getScopeType(),
        plan.getScopeId(),
        planMapper.toDetailResponse(plan),
        vehicleClass);
  }

  public PricingPlan requireById(UUID planId) {
    return planRepository
        .findById(planId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    ResponseCode.RESOURCE_NOT_FOUND, "Pricing plan not found: " + planId));
  }
}
