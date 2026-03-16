package com.ebikes.routing.controllers;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ebikes.routing.dtos.requests.routing.CreateMatrixRequest;
import com.ebikes.routing.dtos.responses.api.SuccessResponse;
import com.ebikes.routing.dtos.responses.routing.MatrixResponse;
import com.ebikes.routing.services.routing.RoutingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/matrices")
@RequiredArgsConstructor
public class MatrixController {

  private final RoutingService routingService;

  @PostMapping
  public SuccessResponse<MatrixResponse> computeMatrix(
      @Valid @RequestBody CreateMatrixRequest request) {
    return SuccessResponse.of(routingService.matrix(request));
  }
}
