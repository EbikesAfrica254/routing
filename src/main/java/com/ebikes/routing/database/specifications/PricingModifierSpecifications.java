package com.ebikes.routing.database.specifications;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import com.ebikes.routing.database.entities.PricingModifier;
import com.ebikes.routing.dtos.requests.filters.PricingModifierFilter;
import com.ebikes.routing.enums.ModifierStackingMode;
import com.ebikes.routing.enums.OrderType;
import com.ebikes.routing.enums.PricingModifierType;

public final class PricingModifierSpecifications {

  public static final String FIELD_ACTIVATED_AT = "activatedAt";
  public static final String FIELD_APPLIES_TO_ORDER_TYPE = "appliesToOrderType";
  public static final String FIELD_ARCHIVED_AT = "archivedAt";
  public static final String FIELD_CREATED_AT = "createdAt";
  public static final String FIELD_EFFECTIVE_FROM = "effectiveFrom";
  public static final String FIELD_EFFECTIVE_TO = "effectiveTo";
  public static final String FIELD_MODIFIER_TYPE = "modifierType";
  public static final String FIELD_PRIORITY = "priority";
  public static final String FIELD_STACKING_MODE = "stackingMode";
  public static final String FIELD_STATUS = "status";
  public static final String FIELD_VEHICLE_CLASS = "vehicleClass";
  public static final String FIELD_VERSION = "version";

  public static final Set<String> ALLOWED_SORT_FIELDS =
      Set.of(
          FIELD_ACTIVATED_AT,
          FIELD_ARCHIVED_AT,
          FIELD_CREATED_AT,
          FIELD_EFFECTIVE_FROM,
          FIELD_EFFECTIVE_TO,
          FIELD_MODIFIER_TYPE,
          FIELD_PRIORITY,
          FIELD_STACKING_MODE,
          FIELD_STATUS,
          FIELD_VEHICLE_CLASS,
          FIELD_VERSION);

  private PricingModifierSpecifications() {
    // prevent instantiation
  }

  public static Specification<PricingModifier> buildSpecification(PricingModifierFilter filter) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      PricingConfigSpecifications.buildSharedPredicates(
          predicates, root, query, criteriaBuilder, filter);
      addIfPresent(
          predicates,
          root,
          query,
          criteriaBuilder,
          filter.getAppliesToOrderType(),
          hasAppliesToOrderType(filter.getAppliesToOrderType()));
      addIfPresent(
          predicates,
          root,
          query,
          criteriaBuilder,
          filter.getModifierType(),
          hasModifierType(filter.getModifierType()));
      addIfPresent(
          predicates,
          root,
          query,
          criteriaBuilder,
          filter.getPriorityMax(),
          hasPriorityMax(filter.getPriorityMax()));
      addIfPresent(
          predicates,
          root,
          query,
          criteriaBuilder,
          filter.getPriorityMin(),
          hasPriorityMin(filter.getPriorityMin()));
      addIfPresent(
          predicates,
          root,
          query,
          criteriaBuilder,
          filter.getStackingMode(),
          hasStackingMode(filter.getStackingMode()));

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }

  public static Specification<PricingModifier> hasAppliesToOrderType(OrderType orderType) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get(FIELD_APPLIES_TO_ORDER_TYPE), orderType);
  }

  public static Specification<PricingModifier> hasModifierType(PricingModifierType modifierType) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get(FIELD_MODIFIER_TYPE), modifierType);
  }

  public static Specification<PricingModifier> hasPriorityMax(Integer priorityMax) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.lessThanOrEqualTo(root.get(FIELD_PRIORITY), priorityMax);
  }

  public static Specification<PricingModifier> hasPriorityMin(Integer priorityMin) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.greaterThanOrEqualTo(root.get(FIELD_PRIORITY), priorityMin);
  }

  public static Specification<PricingModifier> hasStackingMode(ModifierStackingMode stackingMode) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get(FIELD_STACKING_MODE), stackingMode);
  }

  private static void addIfPresent(
      List<Predicate> predicates,
      Root<PricingModifier> root,
      CriteriaQuery<?> query,
      CriteriaBuilder criteriaBuilder,
      Object value,
      Specification<PricingModifier> spec) {
    if (value != null && !(value instanceof String s && s.isBlank())) {
      predicates.add(spec.toPredicate(root, query, criteriaBuilder));
    }
  }
}
