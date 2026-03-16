package com.ebikes.routing.controllers;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ebikes.routing.dtos.requests.pricing.CreatePricingPlanRequest;
import com.ebikes.routing.dtos.requests.pricing.UpdatePricingPlanRequest;
import com.ebikes.routing.dtos.responses.api.SuccessResponse;
import com.ebikes.routing.dtos.responses.pricing.PricingPlanDetailResponse;
import com.ebikes.routing.dtos.responses.pricing.ResolvedPricingResponse;
import com.ebikes.routing.enums.OrderType;
import com.ebikes.routing.enums.VehicleClass;
import com.ebikes.routing.services.pricing.PricingPlanService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/pricing-plans")
@RequiredArgsConstructor
public class PricingPlanController {

  private final PricingPlanService pricingPlanService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAnyAuthority('SYSTEM_ADMIN','ORGANIZATION_ADMIN','BRANCH_ADMIN')")
  public SuccessResponse<PricingPlanDetailResponse> createPlan(
      @Valid @RequestBody CreatePricingPlanRequest request) {
    return SuccessResponse.of(pricingPlanService.createPlan(request));
  }

  @PostMapping("/{id}/activate")
  @PreAuthorize("hasAnyAuthority('SYSTEM_ADMIN','ORGANIZATION_ADMIN','BRANCH_ADMIN')")
  public SuccessResponse<PricingPlanDetailResponse> activatePlan(@PathVariable UUID id) {
    return SuccessResponse.of(pricingPlanService.activatePlan(id));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyAuthority('SYSTEM_ADMIN','ORGANIZATION_ADMIN','BRANCH_ADMIN')")
  public SuccessResponse<PricingPlanDetailResponse> updatePlan(
      @PathVariable UUID id, @Valid @RequestBody UpdatePricingPlanRequest request) {
    return SuccessResponse.of(pricingPlanService.updatePlan(id, request));
  }

  @GetMapping("/resolve")
  public SuccessResponse<ResolvedPricingResponse> resolvePlan(
      @RequestParam VehicleClass vehicleClass,
      @RequestParam(required = false) UUID organizationId,
      @RequestParam(required = false) UUID branchId,
      @RequestParam OrderType orderType) {
    return SuccessResponse.of(
        pricingPlanService.resolvePlan(vehicleClass, organizationId, branchId, orderType));
  }
}
