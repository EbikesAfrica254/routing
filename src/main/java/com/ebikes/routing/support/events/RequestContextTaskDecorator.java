package com.ebikes.routing.support.events;

import org.jspecify.annotations.NonNull;
import org.springframework.core.task.TaskDecorator;

import com.ebikes.routing.support.context.ExecutionContext;

public class RequestContextTaskDecorator implements TaskDecorator {

  @Override
  @NonNull public Runnable decorate(@NonNull Runnable runnable) {
    ExecutionContext.ContextData snapshot = ExecutionContext.snapshot();

    return () -> {
      try {
        ExecutionContext.restore(snapshot);
        runnable.run();
      } finally {
        ExecutionContext.clear();
      }
    };
  }
}
