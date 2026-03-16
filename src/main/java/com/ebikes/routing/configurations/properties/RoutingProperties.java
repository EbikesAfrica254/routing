package com.ebikes.routing.configurations.properties;

import jakarta.validation.constraints.Positive;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "routing")
@Validated
@Data
public class RoutingProperties {
  private ValhallaProperties valhalla;
  private FallbackProperties fallback;
  private MatrixProperties matrix;

  @Data
  public static class ValhallaProperties {
    private String baseUrl;
    private int timeoutSeconds;
  }

  @Data
  public static class FallbackProperties {
    private AverageSpeedProperties averageSpeedKmh;
  }

  @Data
  public static class AverageSpeedProperties {
    private double auto;
    private double bicycle;
  }

  @Data
  public static class MatrixProperties {
    @Positive private int maxAgents;
  }
}
