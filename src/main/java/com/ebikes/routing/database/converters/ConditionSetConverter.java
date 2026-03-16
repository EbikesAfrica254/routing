package com.ebikes.routing.database.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import com.ebikes.routing.domain.ConditionSet;

import tools.jackson.databind.ObjectMapper;

@Converter
public class ConditionSetConverter implements AttributeConverter<ConditionSet, String> {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Override
  public String convertToDatabaseColumn(ConditionSet conditionSet) {
    if (conditionSet == null) {
      return null;
    }
    try {
      return MAPPER.writeValueAsString(conditionSet);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to serialize ConditionSet to JSON", e);
    }
  }

  @Override
  public ConditionSet convertToEntityAttribute(String json) {
    if (json == null || json.isBlank()) {
      return null;
    }
    try {
      return MAPPER.readValue(json, ConditionSet.class);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to deserialize ConditionSet from JSON", e);
    }
  }
}
