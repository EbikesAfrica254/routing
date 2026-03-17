package com.ebikes.routing.services.routing;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import com.ebikes.routing.adapters.ValhallaRoutingAdapter;
import com.ebikes.routing.configurations.properties.RoutingProperties;
import com.ebikes.routing.database.entities.VehicleRoutingProfile;
import com.ebikes.routing.database.repositories.VehicleRoutingProfileRepository;
import com.ebikes.routing.dtos.internal.LocationDto;
import com.ebikes.routing.dtos.internal.RouteResult;
import com.ebikes.routing.dtos.internal.valhalla.ValhallaRouteResponse;
import com.ebikes.routing.dtos.requests.routing.CreateMatrixRequest;
import com.ebikes.routing.dtos.responses.routing.MatrixEntryResponse;
import com.ebikes.routing.dtos.responses.routing.MatrixResponse;
import com.ebikes.routing.enums.ResponseCode;
import com.ebikes.routing.enums.RouteComputationMode;
import com.ebikes.routing.enums.RoutingEngine;
import com.ebikes.routing.exceptions.ResourceNotFoundException;
import com.ebikes.routing.exceptions.ValidationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class RoutingService {

  private static final double METERS_PER_KM = 1000.0;
  private static final double SECONDS_PER_HOUR = 3600.0;
  private static final double EARTH_RADIUS_METERS = 6_371_000.0;

  private final RoutingProperties routingProperties;
  private final ValhallaRoutingAdapter valhallaAdapter;
  private final VehicleRoutingProfileRepository vehicleRoutingProfileRepository;

  public MatrixResponse matrix(CreateMatrixRequest request) {
    int maxAgents = routingProperties.getMatrix().getMaxAgents();
    if (request.agents().size() > maxAgents) {
      throw new ValidationException(
          ResponseCode.INVALID_ARGUMENTS,
          "Agent count exceeds maximum allowed: " + maxAgents,
          "agents",
          request.agents().size());
    }

    VehicleRoutingProfile profile =
        vehicleRoutingProfileRepository
            .findByVehicleClass(request.vehicleClass())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        ResponseCode.RESOURCE_NOT_FOUND,
                        "No routing profile found for vehicleClass=" + request.vehicleClass()));

    List<MatrixEntryResponse> results =
        request.agents().stream()
            .map(
                agent -> {
                  RouteResult result = route(agent.location(), request.pickupLocation(), profile);
                  return new MatrixEntryResponse(
                      agent.agentId(),
                      result.distanceMeters(),
                      result.durationSeconds(),
                      result.isFallback());
                })
            .toList();

    return new MatrixResponse(request.pickupLocation(), results, request.vehicleClass());
  }

  public RouteResult route(
      LocationDto origin, LocationDto destination, VehicleRoutingProfile profile) {

    try {
      ValhallaRouteResponse response =
          valhallaAdapter.route(origin, destination, profile.getEngineCostingKey());

      ValhallaRouteResponse.Summary summary = response.trip().summary();

      int distanceMeters = (int) Math.round(summary.length() * METERS_PER_KM);
      int durationSeconds = (int) Math.round(summary.time());

      log.debug(
          "Valhalla route computed: distanceMeters={}, durationSeconds={}, costing={}",
          distanceMeters,
          durationSeconds,
          profile.getEngineCostingKey());

      return new RouteResult(
          distanceMeters,
          durationSeconds,
          profile.getEngineCostingKey(),
          false,
          RouteComputationMode.ROUTED,
          RoutingEngine.VALHALLA);

    } catch (RestClientException ex) {
      log.warn(
          "Valhalla routing failed — costing={}, error={}",
          profile.getEngineCostingKey(),
          ex.getMessage());

      if (!profile.isFallbackEnabled()) {
        throw new ValidationException(
            ResponseCode.EXTERNAL_SERVICE_ERROR,
            "Routing service unavailable and fallback is disabled for vehicleClass="
                + profile.getVehicleClass(),
            "vehicleClass",
            profile.getVehicleClass());
      }

      return computeHaversineFallback(origin, destination, profile);
    }
  }

  private RouteResult computeHaversineFallback(
      LocationDto origin, LocationDto destination, VehicleRoutingProfile profile) {

    double distanceMeters = haversineMeters(origin, destination);
    int adjustedMeters =
        (int) Math.round(distanceMeters * profile.getFallbackMultiplier().doubleValue());

    double averageSpeedKmh = resolveAverageSpeedKmh(profile.getEngineCostingKey());
    double distanceKm = adjustedMeters / METERS_PER_KM;
    int durationSeconds = (int) Math.round((distanceKm / averageSpeedKmh) * SECONDS_PER_HOUR);

    log.debug(
        "Haversine fallback computed: distanceMeters={}, durationSeconds={}, costing={}",
        adjustedMeters,
        durationSeconds,
        profile.getEngineCostingKey());

    return new RouteResult(
        adjustedMeters,
        durationSeconds,
        profile.getEngineCostingKey(),
        true,
        RouteComputationMode.FALLBACK,
        RoutingEngine.HAVERSINE_FALLBACK);
  }

  private double haversineMeters(LocationDto origin, LocationDto destination) {
    double lat1 = Math.toRadians(origin.latitude().doubleValue());
    double lat2 = Math.toRadians(destination.latitude().doubleValue());
    double dLat = lat2 - lat1;
    double dLon =
        Math.toRadians(destination.longitude().doubleValue() - origin.longitude().doubleValue());

    double a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);

    return EARTH_RADIUS_METERS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  }

  private double resolveAverageSpeedKmh(String costingKey) {
    return switch (costingKey) {
      case "bicycle" -> routingProperties.getFallback().getAverageSpeedKmh().getBicycle();
      case "auto" -> routingProperties.getFallback().getAverageSpeedKmh().getAuto();
      default -> throw new IllegalStateException("Unknown costing key: " + costingKey);
    };
  }
}
