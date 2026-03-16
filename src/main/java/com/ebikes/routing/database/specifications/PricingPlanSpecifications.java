package com.ebikes.routing.database.specifications;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;

import com.ebikes.routing.database.entities.PricingPlan;
import com.ebikes.routing.dtos.requests.filters.PricingPlanFilter;

public final class PricingPlanSpecifications {

  public static final String FIELD_ACTIVATED_AT = "activatedAt";
  public static final String FIELD_ARCHIVED_AT = "archivedAt";
  public static final String FIELD_CREATED_AT = "createdAt";
  public static final String FIELD_EFFECTIVE_FROM = "effectiveFrom";
  public static final String FIELD_EFFECTIVE_TO = "effectiveTo";
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
          FIELD_STATUS,
          FIELD_VEHICLE_CLASS,
          FIELD_VERSION);

  private PricingPlanSpecifications() {
    // prevent instantiation
  }

  public static Specification<PricingPlan> buildSpecification(PricingPlanFilter filter) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      PricingConfigSpecifications.buildSharedPredicates(
          predicates, root, query, criteriaBuilder, filter);

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
