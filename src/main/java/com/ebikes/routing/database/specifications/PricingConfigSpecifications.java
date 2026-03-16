package com.ebikes.routing.database.specifications;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import com.ebikes.routing.dtos.requests.filters.bases.PricingConfigFilter;
import com.ebikes.routing.enums.PricingStatus;
import com.ebikes.routing.enums.ScopeType;
import com.ebikes.routing.enums.VehicleClass;
import com.ebikes.routing.support.database.FilterUtilities;

public final class PricingConfigSpecifications {

  private PricingConfigSpecifications() {
    // prevent instantiation
  }

  public static <T> void buildSharedPredicates(
      List<Predicate> predicates,
      Root<T> root,
      CriteriaQuery<?> query,
      CriteriaBuilder criteriaBuilder,
      PricingConfigFilter filter) {
    FilterUtilities.addDateRange(
        predicates,
        root,
        criteriaBuilder,
        "activatedAt",
        filter.getActivatedAtAfter(),
        filter.getActivatedAtBefore());
    FilterUtilities.addDateRange(
        predicates,
        root,
        criteriaBuilder,
        "archivedAt",
        filter.getArchivedAtAfter(),
        filter.getArchivedAtBefore());
    FilterUtilities.addDateRange(
        predicates,
        root,
        criteriaBuilder,
        "createdAt",
        filter.getCreatedAtAfter(),
        filter.getCreatedAtBefore());
    FilterUtilities.addDateRange(
        predicates,
        root,
        criteriaBuilder,
        "effectiveFrom",
        filter.getEffectiveFromAfter(),
        filter.getEffectiveFromBefore());
    FilterUtilities.addDateRange(
        predicates,
        root,
        criteriaBuilder,
        "effectiveTo",
        filter.getEffectiveToAfter(),
        filter.getEffectiveToBefore());
    addIfPresent(
        predicates,
        root,
        query,
        criteriaBuilder,
        filter.getScopeId(),
        hasScopeId(filter.getScopeId()));
    addIfPresent(
        predicates,
        root,
        query,
        criteriaBuilder,
        filter.getScopeType(),
        hasScopeType(filter.getScopeType()));
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
    addIfPresent(
        predicates,
        root,
        query,
        criteriaBuilder,
        filter.getVersion(),
        hasVersion(filter.getVersion()));
  }

  public static <T> Specification<T> hasScopeId(UUID scopeId) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("scopeId"), scopeId);
  }

  public static <T> Specification<T> hasScopeType(ScopeType scopeType) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get("scopeType"), scopeType);
  }

  public static <T> Specification<T> hasStatus(PricingStatus status) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status);
  }

  public static <T> Specification<T> hasVehicleClass(VehicleClass vehicleClass) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get("vehicleClass"), vehicleClass);
  }

  public static <T> Specification<T> hasVersion(String version) {
    return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("version"), version);
  }

  private static <T> void addIfPresent(
      List<Predicate> predicates,
      Root<T> root,
      CriteriaQuery<?> query,
      CriteriaBuilder criteriaBuilder,
      Object value,
      Specification<T> spec) {
    if (value != null && !(value instanceof String s && s.isBlank())) {
      predicates.add(spec.toPredicate(root, query, criteriaBuilder));
    }
  }
}
