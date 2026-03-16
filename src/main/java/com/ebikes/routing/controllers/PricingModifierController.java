package com.ebikes.routing.controllers;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ebikes.routing.dtos.requests.filters.PricingModifierFilter;
import com.ebikes.routing.dtos.requests.pricing.CreatePricingModifierRequest;
import com.ebikes.routing.dtos.requests.pricing.UpdatePricingModifierRequest;
import com.ebikes.routing.dtos.responses.api.PaginatedResponse;
import com.ebikes.routing.dtos.responses.api.SuccessResponse;
import com.ebikes.routing.dtos.responses.pricing.PricingModifierDetailResponse;
import com.ebikes.routing.dtos.responses.pricing.PricingModifierSummaryResponse;
import com.ebikes.routing.services.pricing.PricingModifierService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/pricing-modifiers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('SYSTEM_ADMIN','ORGANIZATION_ADMIN','BRANCH_ADMIN')")
public class PricingModifierController {

  private final PricingModifierService pricingModifierService;

  @PostMapping("/{id}/activate")
  public SuccessResponse<PricingModifierDetailResponse> activateModifier(@PathVariable UUID id) {
    return SuccessResponse.of(pricingModifierService.activateModifier(id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public SuccessResponse<PricingModifierDetailResponse> createModifier(
      @Valid @RequestBody CreatePricingModifierRequest request) {
    return SuccessResponse.of(pricingModifierService.createModifier(request));
  }

  @GetMapping
  @PreAuthorize("hasAnyAuthority('SYSTEM_ADMIN','ORGANIZATION_ADMIN','BRANCH_ADMIN')")
  public PaginatedResponse<PricingModifierSummaryResponse> searchModifiers(
      @Valid @ModelAttribute PricingModifierFilter filter) {
    return pricingModifierService.searchPricingModifiers(filter);
  }

  @PutMapping("/{id}")
  public SuccessResponse<PricingModifierDetailResponse> updateModifier(
      @PathVariable UUID id, @Valid @RequestBody UpdatePricingModifierRequest request) {
    return SuccessResponse.of(pricingModifierService.updateModifier(id, request));
  }
}
