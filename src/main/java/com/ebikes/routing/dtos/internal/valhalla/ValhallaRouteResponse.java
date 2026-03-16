package com.ebikes.routing.dtos.internal.valhalla;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ValhallaRouteResponse(@JsonProperty("trip") Trip trip) {

  public record Trip(
      @JsonProperty("legs") List<Leg> legs, @JsonProperty("summary") Summary summary) {}

  public record Leg(@JsonProperty("summary") Summary summary) {}

  public record Summary(@JsonProperty("length") double length, @JsonProperty("time") double time) {}
}
