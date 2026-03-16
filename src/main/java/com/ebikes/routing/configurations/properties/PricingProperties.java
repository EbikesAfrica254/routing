package com.ebikes.routing.configurations.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "pricing")
@Validated
@Data
public class PricingProperties {
  @Positive private int previewExpiryMinutes;
  @NotBlank private String zoneId;
}
