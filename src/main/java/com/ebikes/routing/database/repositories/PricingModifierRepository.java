package com.ebikes.routing.database.repositories;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ebikes.routing.database.entities.PricingModifier;
import com.ebikes.routing.enums.OrderType;
import com.ebikes.routing.enums.ScopeType;
import com.ebikes.routing.enums.VehicleClass;

public interface PricingModifierRepository
    extends JpaRepository<PricingModifier, UUID>, JpaSpecificationExecutor<PricingModifier> {

  @Query(
      """
      SELECT m FROM PricingModifier m
      WHERE m.status = 'ACTIVE'
      AND m.effectiveFrom <= :now
      AND (m.effectiveTo IS NULL OR m.effectiveTo > :now)
      AND (m.vehicleClass IS NULL OR m.vehicleClass = :vehicleClass)
      AND (m.appliesToOrderType IS NULL OR m.appliesToOrderType = :orderType)
      AND (
          m.scopeType = 'GLOBAL'
          OR (m.scopeType = 'ORGANIZATION' AND m.scopeId = :organizationId)
          OR (m.scopeType = 'BRANCH' AND m.scopeId = :branchId)
      )
      ORDER BY m.priority ASC
      """)
  List<PricingModifier> findActiveModifiersForScopes(
      @Param("vehicleClass") VehicleClass vehicleClass,
      @Param("orderType") OrderType orderType,
      @Param("organizationId") UUID organizationId,
      @Param("branchId") UUID branchId,
      @Param("now") OffsetDateTime now);

  @Query(
      """
      SELECT COUNT(m) > 0 FROM PricingModifier m
      WHERE m.status = 'ACTIVE'
      AND m.scopeType = :scopeType
      AND m.scopeId = :scopeId
      AND m.modifierType = :modifierType
      AND m.priority = :priority
      AND (m.effectiveTo IS NULL OR m.effectiveTo > :now)
      """)
  boolean existsActiveConflictForScope(
      @Param("scopeType") ScopeType scopeType,
      @Param("scopeId") UUID scopeId,
      @Param("modifierType") com.ebikes.routing.enums.PricingModifierType modifierType,
      @Param("priority") int priority,
      @Param("now") OffsetDateTime now);
}
