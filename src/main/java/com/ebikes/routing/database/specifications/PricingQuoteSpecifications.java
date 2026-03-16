package com.ebikes.routing.database.specifications;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import com.ebikes.routing.database.entities.PricingQuote;
import com.ebikes.routing.dtos.requests.filters.PricingQuoteFilter;
import com.ebikes.routing.enums.OrderType;
import com.ebikes.routing.enums.PricingQuoteStatus;
import com.ebikes.routing.enums.RouteComputationMode;
import com.ebikes.routing.enums.RoutingEngine;
import com.ebikes.routing.enums.VehicleClass;
import com.ebikes.routing.support.database.FilterUtilities;

public class PricingQuoteSpecifications {

  public static final String FIELD_BRANCH_ID = "branchId";
  public static final String FIELD_CREATED_AT = "createdAt";
  public static final String FIELD_FINAL_AMOUNT = "finalAmount";
  public static final String FIELD_IS_COMMITTED = "isCommitted";
  public static final String FIELD_IS_FALLBACK = "isFallback";
  public static final String FIELD_ORDER_ID = "orderId";
  public static final String FIELD_ORDER_TYPE = "orderType";
  public static final String FIELD_ORGANIZATION_ID = "organizationId";
  public static final String FIELD_PRICING_PLAN_ID = "pricingPlanId";
  public static final String FIELD_PRICING_PLAN_VERSION = "pricingPlanVersion";
  public static final String FIELD_ROUTE_COMPUTATION_MODE = "routeComputationMode";
  public static final String FIELD_ROUTING_ENGINE = "routingEngine";
  public static final String FIELD_STATUS = "status";
  public static final String FIELD_VEHICLE_CLASS = "vehicleClass";

  public static final Set<String> ALLOWED_SORT_FIELDS =
      Set.of(
          FIELD_CREATED_AT,
          FIELD_FINAL_AMOUNT,
          FIELD_ORDER_TYPE,
          FIELD_STATUS,
          FIELD_VEHICLE_CLASS);

  private PricingQuoteSpecifications() {
    // prevent instantiation
  }

  public static Specification<PricingQuote> buildSpecification(PricingQuoteFilter filter) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      addIfPresent(
          predicates,
          root,
          query,
          criteriaBuilder,
          filter.getBranchId(),
          hasBranchId(filter.getBranchId()));
      addIfPresent(
          predicates,
          root,
          query,
          criteriaBuilder,
          filter.getCreatedAtAfter(),
          offsetDateTimeAfter(FIELD_CREATED_AT, filter.getCreatedAtAfter()));
      addIfPresent(
          predicates,
          root,
          query,
          criteriaBuilder,
          filter.getCreatedAtBefore(),
          offsetDateTimeBefore(FIELD_CREATED_AT, filter.getCreatedAtBefore()));
      addIfPresent(
          predicates,
          root,
          query,
          criteriaBuilder,
          filter.getFinalAmountMax(),
          hasFinalAmountMax(filter.getFinalAmountMax()));
      addIfPresent(
          predicates,
          root,
          query,
          criteriaBuilder,
          filter.getFinalAmountMin(),
          hasFinalAmountMin(filter.getFinalAmountMin()));
      addIfPresent(
          predicates,
          root,
          query,
          criteriaBuilder,
          filter.getIsCommitted(),
          hasIsCommitted(filter.getIsCommitted()));
      addIfPresent(
          predicates,
          root,
          query,
          criteriaBuilder,
          filter.getIsFallback(),
          hasIsFallback(filter.getIsFallback()));
      addIfPresent(
          predicates,
          root,
          query,
          criteriaBuilder,
          filter.getOrderId(),
          hasOrderId(filter.getOrderId()));
      addIfPresent(
          predicates,
          root,
          query,
          criteriaBuilder,
          filter.getOrderType(),
          hasOrderType(filter.getOrderType()));
      addIfPresent(
          predicates,
          root,
          query,
          criteriaBuilder,
          filter.getOrganizationId(),
          hasOrganizationId(filter.getOrganizationId()));
      addIfPresent(
          predicates,
          root,
          query,
          criteriaBuilder,
          filter.getPricingPlanId(),
          hasPricingPlanId(filter.getPricingPlanId()));
      addIfPresent(
          predicates,
          root,
          query,
          criteriaBuilder,
          filter.getPricingPlanVersion(),
          hasPricingPlanVersion(filter.getPricingPlanVersion()));
      addIfPresent(
          predicates,
          root,
          query,
          criteriaBuilder,
          filter.getRouteComputationMode(),
          hasRouteComputationMode(filter.getRouteComputationMode()));
      addIfPresent(
          predicates,
          root,
          query,
          criteriaBuilder,
          filter.getRoutingEngine(),
          hasRoutingEngine(filter.getRoutingEngine()));
      addIfPresent(
          predicates,
          root,
          query,
          criteriaBuilder,
          filter.getStatus(),
          hasStatus(filter.getStatus()));
      addIfPresent(
          predicates,
          root,
          query,
          criteriaBuilder,
          filter.getVehicleClass(),
          hasVehicleClass(filter.getVehicleClass()));

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }

  public static Specification<PricingQuote> hasBranchId(UUID branchId) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get(FIELD_BRANCH_ID), branchId);
  }

  public static Specification<PricingQuote> hasFinalAmountMax(BigDecimal finalAmountMax) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.lessThanOrEqualTo(root.get(FIELD_FINAL_AMOUNT), finalAmountMax);
  }

  public static Specification<PricingQuote> hasFinalAmountMin(BigDecimal finalAmountMin) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.greaterThanOrEqualTo(root.get(FIELD_FINAL_AMOUNT), finalAmountMin);
  }

  public static Specification<PricingQuote> hasIsCommitted(Boolean isCommitted) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get(FIELD_IS_COMMITTED), isCommitted);
  }

  public static Specification<PricingQuote> hasIsFallback(Boolean isFallback) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get(FIELD_IS_FALLBACK), isFallback);
  }

  public static Specification<PricingQuote> hasOrderId(UUID orderId) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get(FIELD_ORDER_ID), orderId);
  }

  public static Specification<PricingQuote> hasOrderType(OrderType orderType) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get(FIELD_ORDER_TYPE), orderType);
  }

  public static Specification<PricingQuote> hasOrganizationId(UUID organizationId) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get(FIELD_ORGANIZATION_ID), organizationId);
  }

  public static Specification<PricingQuote> hasPricingPlanId(UUID pricingPlanId) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get(FIELD_PRICING_PLAN_ID), pricingPlanId);
  }

  public static Specification<PricingQuote> hasPricingPlanVersion(String pricingPlanVersion) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get(FIELD_PRICING_PLAN_VERSION), pricingPlanVersion);
  }

  public static Specification<PricingQuote> hasRouteComputationMode(
      RouteComputationMode routeComputationMode) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get(FIELD_ROUTE_COMPUTATION_MODE), routeComputationMode);
  }

  public static Specification<PricingQuote> hasRoutingEngine(RoutingEngine routingEngine) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get(FIELD_ROUTING_ENGINE), routingEngine);
  }

  public static Specification<PricingQuote> hasStatus(PricingQuoteStatus status) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(FIELD_STATUS), status);
  }

  public static Specification<PricingQuote> hasVehicleClass(VehicleClass vehicleClass) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get(FIELD_VEHICLE_CLASS), vehicleClass);
  }

  public static Specification<PricingQuote> offsetDateTimeAfter(
      String fieldPath, OffsetDateTime after) {
    return FilterUtilities.offsetDateTimeAfter(fieldPath, after);
  }

  public static Specification<PricingQuote> offsetDateTimeBefore(
      String fieldPath, OffsetDateTime before) {
    return FilterUtilities.offsetDateTimeBefore(fieldPath, before);
  }

  private static void addIfPresent(
      List<Predicate> predicates,
      Root<PricingQuote> root,
      CriteriaQuery<?> query,
      CriteriaBuilder criteriaBuilder,
      Object value,
      Specification<PricingQuote> spec) {
    if (value != null && !(value instanceof String s && s.isBlank())) {
      predicates.add(spec.toPredicate(root, query, criteriaBuilder));
    }
  }
}
