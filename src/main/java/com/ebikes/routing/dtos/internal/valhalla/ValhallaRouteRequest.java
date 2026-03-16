package com.ebikes.routing.dtos.internal.valhalla;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ValhallaRouteRequest(
    List<ValhallaLocation> locations,
    String costing,
    @JsonProperty("directions_options") DirectionsOptions directionsOptions) {

  public record ValhallaLocation(double lat, double lon) {}

  public record DirectionsOptions(String units) {}
}
