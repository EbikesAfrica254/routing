package com.ebikes.routing.constants;

import com.ebikes.routing.support.references.ReferenceGenerator;

import lombok.experimental.UtilityClass;

@UtilityClass
public class EventConstants {

  public static final class EventSource {

    private EventSource() {
      throw new UnsupportedOperationException(ApplicationConstants.CLASS_CANNOT_BE_INSTANTIATED);
    }

    public static final String HOST_SERVICE = "routing";

    public static String serviceReference() {
      return ReferenceGenerator.generateServiceReference(HOST_SERVICE);
    }
  }

  public static final class EventTypes {

    private EventTypes() {
      throw new UnsupportedOperationException(ApplicationConstants.CLASS_CANNOT_BE_INSTANTIATED);
    }

    public static final class PricingPlans {

      private PricingPlans() {
        throw new UnsupportedOperationException(ApplicationConstants.CLASS_CANNOT_BE_INSTANTIATED);
      }

      public static final String ACTIVATED = EventSource.HOST_SERVICE + ".pricing-plan.activated";
      public static final String CREATED = EventSource.HOST_SERVICE + ".pricing-plan.created";
    }

    public static final class PricingModifiers {

      private PricingModifiers() {
        throw new UnsupportedOperationException(ApplicationConstants.CLASS_CANNOT_BE_INSTANTIATED);
      }

      public static final String ACTIVATED =
          EventSource.HOST_SERVICE + ".pricing-modifier.activated";
      public static final String CREATED = EventSource.HOST_SERVICE + ".pricing-modifier.created";
    }

    public static final class PricingQuotes {

      private PricingQuotes() {
        throw new UnsupportedOperationException(ApplicationConstants.CLASS_CANNOT_BE_INSTANTIATED);
      }

      public static final String CREATED = EventSource.HOST_SERVICE + ".pricing-quote.created";
    }
  }

  public static final class RoutingKeys {

    private RoutingKeys() {
      throw new UnsupportedOperationException(ApplicationConstants.CLASS_CANNOT_BE_INSTANTIATED);
    }

    // outbound — audit routing keys
    public static final String PRICING_MODIFIER_AUDIT =
        audit(EventSource.HOST_SERVICE + ".pricing-modifier");
    public static final String PRICING_PLAN_AUDIT =
        audit(EventSource.HOST_SERVICE + ".pricing-plan");
    public static final String PRICING_QUOTE_AUDIT =
        audit(EventSource.HOST_SERVICE + ".pricing-quote");

    // outbound — domain event routing keys
    public static final String PRICING_MODIFIER_ACTIVATED = EventTypes.PricingModifiers.ACTIVATED;
    public static final String PRICING_PLAN_ACTIVATED = EventTypes.PricingPlans.ACTIVATED;
    public static final String PRICING_QUOTE_CREATED = EventTypes.PricingQuotes.CREATED;

    public static String audit(String domain) {
      return domain + ".audit";
    }
  }

  public static final class InboxSourceContext {

    private InboxSourceContext() {
      throw new UnsupportedOperationException(ApplicationConstants.CLASS_CANNOT_BE_INSTANTIATED);
    }

    private static final String DELIMITER = ":";

    public static String getSourceContext(String serviceReference) {
      return serviceReference.split(DELIMITER)[0];
    }
  }

  public static final class MessageHeaders {

    private MessageHeaders() {
      throw new UnsupportedOperationException(ApplicationConstants.CLASS_CANNOT_BE_INSTANTIATED);
    }

    public static final String EVENT_TYPE = "eventType";
    public static final String OUTBOX_ID = "outboxId";
    public static final String ROUTING_KEY = "routingKey";
  }
}
