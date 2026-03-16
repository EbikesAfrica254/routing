package com.ebikes.routing.support.audit;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.ebikes.routing.database.entities.PricingModifier;
import com.ebikes.routing.database.entities.PricingPlan;
import com.ebikes.routing.database.entities.PricingQuote;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AuditMetadataBuilder {

  private final String scopeId = "scopeId";
  private final String scopeType = "scopeType";
  private final String status = "status";
  private final String vehicleClass = "vehicleClass";
  private final String version = "version";

  public static Map<String, String> forPricingPlan(PricingPlan plan) {
    Map<String, String> metadata = new HashMap<>();
    metadata.put("pricingPlanId", plan.getId().toString());
    metadata.put(scopeType, plan.getScopeType().name());
    metadata.put(status, plan.getStatus().name());
    metadata.put(vehicleClass, plan.getVehicleClass().name());
    metadata.put(version, plan.getVersion());
    if (plan.getScopeId() != null) {
      metadata.put(scopeId, plan.getScopeId().toString());
    }
    return Collections.unmodifiableMap(metadata);
  }

  public static Map<String, String> forPricingModifier(PricingModifier modifier) {
    Map<String, String> metadata = new HashMap<>();
    metadata.put("pricingModifierId", modifier.getId().toString());
    metadata.put("modifierType", modifier.getModifierType().name());
    metadata.put("priority", String.valueOf(modifier.getPriority()));
    metadata.put(scopeType, modifier.getScopeType().name());
    metadata.put("stackingMode", modifier.getStackingMode().name());
    metadata.put(status, modifier.getStatus().name());
    metadata.put(version, modifier.getVersion());
    if (modifier.getScopeId() != null) {
      metadata.put(scopeId, modifier.getScopeId().toString());
    }
    if (modifier.getVehicleClass() != null) {
      metadata.put(vehicleClass, modifier.getVehicleClass().name());
    }
    if (modifier.getAppliesToOrderType() != null) {
      metadata.put("appliesToOrderType", modifier.getAppliesToOrderType().name());
    }
    return Collections.unmodifiableMap(metadata);
  }

  public static Map<String, String> forPricingQuote(PricingQuote quote) {
    Map<String, String> metadata = new HashMap<>();
    metadata.put("pricingQuoteId", quote.getId().toString());
    metadata.put("isCommitted", String.valueOf(quote.isCommitted()));
    metadata.put("isFallback", String.valueOf(quote.isFallback()));
    metadata.put("orderType", quote.getOrderType().name());
    metadata.put("pricingPlanVersion", quote.getPricingPlanVersion());
    metadata.put(status, quote.getStatus().name());
    metadata.put(vehicleClass, quote.getVehicleClass().name());
    if (quote.getOrderId() != null) {
      metadata.put("orderId", quote.getOrderId().toString());
    }
    if (quote.getOrganizationId() != null) {
      metadata.put("organizationId", quote.getOrganizationId().toString());
    }
    if (quote.getBranchId() != null) {
      metadata.put("branchId", quote.getBranchId().toString());
    }
    return Collections.unmodifiableMap(metadata);
  }
}
