package com.ebikes.routing.database.repositories;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ebikes.routing.database.entities.PricingPlan;
import com.ebikes.routing.enums.ScopeType;
import com.ebikes.routing.enums.VehicleClass;

public interface PricingPlanRepository
    extends JpaRepository<PricingPlan, UUID>, JpaSpecificationExecutor<PricingPlan> {

  @Query(
      value =
          """
          SELECT * FROM routing.pricing_plans
          WHERE status = 'ACTIVE'
          AND vehicle_class = :vehicleClass
          AND effective_from <= :now
          AND (effective_to IS NULL OR effective_to > :now)
          AND (
              (scope_type = 'BRANCH'       AND scope_id = :branchId)
              OR (scope_type = 'ORGANIZATION' AND scope_id = :organizationId)
              OR (scope_type = 'GLOBAL')
          )
          ORDER BY
              CASE scope_type
                  WHEN 'BRANCH'       THEN 1
                  WHEN 'ORGANIZATION' THEN 2
                  WHEN 'GLOBAL'       THEN 3
              END
          LIMIT 1
          """,
      nativeQuery = true)
  Optional<PricingPlan> findActivePlanForScope(
      @Param("vehicleClass") String vehicleClass,
      @Param("branchId") UUID branchId,
      @Param("organizationId") UUID organizationId,
      @Param("now") OffsetDateTime now);

  @Query(
      """
      SELECT COUNT(p) > 0 FROM PricingPlan p
      WHERE p.status = 'ACTIVE'
      AND p.scopeType = :scopeType
      AND p.scopeId = :scopeId
      AND p.vehicleClass = :vehicleClass
      """)
  boolean existsActiveForScopeAndVehicleClass(
      @Param("scopeType") ScopeType scopeType,
      @Param("scopeId") UUID scopeId,
      @Param("vehicleClass") VehicleClass vehicleClass);
}
