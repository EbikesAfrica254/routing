package com.ebikes.routing.adapters;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.ebikes.routing.configurations.properties.RoutingProperties;
import com.ebikes.routing.dtos.internal.LocationDto;
import com.ebikes.routing.dtos.internal.valhalla.ValhallaRouteRequest;
import com.ebikes.routing.dtos.internal.valhalla.ValhallaRouteRequest.DirectionsOptions;
import com.ebikes.routing.dtos.internal.valhalla.ValhallaRouteRequest.ValhallaLocation;
import com.ebikes.routing.dtos.internal.valhalla.ValhallaRouteResponse;
import com.ebikes.routing.enums.ResponseCode;
import com.ebikes.routing.exceptions.ExternalServiceException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ValhallaRoutingAdapter {

  private final RestClient restClient;

  public ValhallaRoutingAdapter(
      RestClient.Builder restClientBuilder, RoutingProperties routingProperties) {
    this.restClient =
        restClientBuilder
            .baseUrl(routingProperties.getValhalla().getBaseUrl())
            .defaultHeader("Content-Type", "application/json")
            .build();
  }

  public ValhallaRouteResponse route(
      LocationDto origin, LocationDto destination, String costingKey) {

    try {
      ValhallaRouteRequest request =
          new ValhallaRouteRequest(
              List.of(
                  new ValhallaLocation(
                      origin.latitude().doubleValue(), origin.longitude().doubleValue()),
                  new ValhallaLocation(
                      destination.latitude().doubleValue(), destination.longitude().doubleValue())),
              costingKey,
              new DirectionsOptions("kilometers"));

      log.debug(
          "Calling Valhalla: origin={},{} destination={},{} costing={}",
          origin.latitude(),
          origin.longitude(),
          destination.latitude(),
          destination.longitude(),
          costingKey);

      return restClient
          .post()
          .uri("/route")
          .body(request)
          .retrieve()
          .body(ValhallaRouteResponse.class);
    } catch (RestClientException e) {
      log.error("Valhalla service call failed for costingKey={}", costingKey, e);
      throw new ExternalServiceException(
          "/route",
          "Failed to obtain quote from routing service: " + e.getMessage(),
          ResponseCode.fromHttpStatus(extractStatus(e)),
          e);
    }
  }

  private int extractStatus(RestClientException e) {
    if (e instanceof org.springframework.web.client.HttpStatusCodeException statusEx) {
      return statusEx.getStatusCode().value();
    }
    return 500;
  }
}
