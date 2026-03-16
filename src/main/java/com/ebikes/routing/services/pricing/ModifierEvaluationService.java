package com.ebikes.routing.services.pricing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ebikes.routing.database.entities.PricingModifier;
import com.ebikes.routing.domain.ConditionSet;
import com.ebikes.routing.domain.ModifierEvaluationContext;
import com.ebikes.routing.enums.ModifierStackingMode;
import com.ebikes.routing.enums.ResponseCode;
import com.ebikes.routing.exceptions.ValidationException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ModifierEvaluationService {

  public List<PricingModifier> evaluate(
      List<PricingModifier> candidates, ModifierEvaluationContext context) {

    if (candidates.isEmpty()) {
      return List.of();
    }

    List<PricingModifier> conditionPassing =
        candidates.stream().filter(m -> matchesConditions(m, context)).toList();

    if (conditionPassing.isEmpty()) {
      return List.of();
    }

    Map<Integer, List<PricingModifier>> byPriority =
        conditionPassing.stream().collect(Collectors.groupingBy(PricingModifier::getPriority));

    List<PricingModifier> result = new ArrayList<>();

    for (Map.Entry<Integer, List<PricingModifier>> entry :
        byPriority.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {

      List<PricingModifier> group = entry.getValue();
      int priority = entry.getKey();

      long exclusiveCount =
          group.stream().filter(m -> m.getStackingMode() == ModifierStackingMode.EXCLUSIVE).count();

      if (exclusiveCount > 1) {
        throw new ValidationException(
            ResponseCode.INVALID_STATE,
            "Modifier evaluation cannot be completed deterministically: "
                + exclusiveCount
                + " EXCLUSIVE modifiers share priority "
                + priority
                + " — resolve the conflict before activating",
            "priority",
            priority);
      }

      result.addAll(group);
    }

    log.debug(
        "Modifier evaluation complete: candidates={}, passing={}",
        candidates.size(),
        result.size());

    return result;
  }

  private boolean matchesConditions(PricingModifier modifier, ModifierEvaluationContext context) {
    ConditionSet conditionSet = modifier.getConditionSet();

    if (conditionSet == null) {
      return true;
    }

    return conditionSet.matches(context);
  }
}
