package com.ebikes.routing.controllers;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ebikes.routing.dtos.requests.filters.PricingQuoteFilter;
import com.ebikes.routing.dtos.requests.routing.CreateQuoteRequest;
import com.ebikes.routing.dtos.responses.api.PaginatedResponse;
import com.ebikes.routing.dtos.responses.api.SuccessResponse;
import com.ebikes.routing.dtos.responses.pricing.PricingQuoteDetailResponse;
import com.ebikes.routing.dtos.responses.pricing.PricingQuoteSummaryResponse;
import com.ebikes.routing.dtos.responses.pricing.QuoteCreatedResponse;
import com.ebikes.routing.services.pricing.PricingQuoteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class QuoteController {

  private final PricingQuoteService pricingQuoteService;

  @PostMapping("/quotes")
  @ResponseStatus(HttpStatus.CREATED)
  public SuccessResponse<QuoteCreatedResponse> createQuote(
      @Valid @RequestBody CreateQuoteRequest request) {
    return SuccessResponse.of(pricingQuoteService.createQuote(request));
  }

  @GetMapping("/pricing-quotes/{id}")
  @PreAuthorize("hasAnyAuthority('SYSTEM_ADMIN','ORGANIZATION_ADMIN','BRANCH_ADMIN')")
  public SuccessResponse<PricingQuoteDetailResponse> getQuote(@PathVariable UUID id) {
    return SuccessResponse.of(pricingQuoteService.getQuote(id));
  }

  @GetMapping("/pricing-quotes")
  @PreAuthorize("hasAnyAuthority('SYSTEM_ADMIN','ORGANIZATION_ADMIN','BRANCH_ADMIN')")
  public PaginatedResponse<PricingQuoteSummaryResponse> searchQuotes(
      @Valid @ModelAttribute PricingQuoteFilter filter) {
    return pricingQuoteService.searchQuotes(filter);
  }
}
