package com.ebikes.routing.services.events;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ebikes.routing.database.entities.Outbox;
import com.ebikes.routing.database.repositories.OutboxRepository;
import com.ebikes.routing.database.specifications.OutboxSpecifications;
import com.ebikes.routing.dtos.requests.filters.OutboxFilter;
import com.ebikes.routing.dtos.responses.api.PaginatedResponse;
import com.ebikes.routing.dtos.responses.outbox.OutboxResponse;
import com.ebikes.routing.enums.OutboxStatus;
import com.ebikes.routing.enums.ResponseCode;
import com.ebikes.routing.exceptions.ResourceNotFoundException;
import com.ebikes.routing.mappers.OutboxMapper;
import com.ebikes.routing.support.database.FilterUtilities;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class OutboxService {

  private static final Set<String> ALLOWED_SORT_FIELDS =
      Set.of(
          OutboxSpecifications.FIELD_CREATED_AT,
          OutboxSpecifications.FIELD_EVENT_TYPE,
          OutboxSpecifications.FIELD_RETRY_COUNT,
          OutboxSpecifications.FIELD_STATUS,
          OutboxSpecifications.FIELD_UPDATED_AT);

  private final OutboxMapper mapper;
  private final OutboxRepository repository;

  @Transactional
  public void retry(UUID outboxId) {
    log.info("Retrying failed outbox event: outboxId={}", outboxId);

    Outbox outbox = requireById(outboxId);
    outbox.resetForRetry();
    repository.save(outbox);

    log.info("Outbox event reset to PENDING: outboxId={}", outboxId);
  }

  @Transactional
  public int retryAllFailed() {
    log.info("Retrying all failed outbox events");

    List<Outbox> failedEvents = repository.findByStatusOrderByIdAsc(OutboxStatus.FAILED);
    failedEvents.forEach(Outbox::resetForRetry);
    repository.saveAll(failedEvents);

    log.info("Reset {} failed outbox events to PENDING", failedEvents.size());

    return failedEvents.size();
  }

  @Transactional
  public void save(String eventType, Serializable payload, String routingKey) {
    Outbox outbox =
        Outbox.builder().eventType(eventType).payload(payload).routingKey(routingKey).build();

    repository.save(outbox);

    log.debug("Outbox record created: eventType={}, outboxId={}", eventType, outbox.getId());
  }

  @Transactional(readOnly = true)
  public PaginatedResponse<OutboxResponse> search(OutboxFilter filter) {
    Specification<Outbox> spec = OutboxSpecifications.buildSpecification(filter);
    Pageable pageable = FilterUtilities.buildPageable(filter, ALLOWED_SORT_FIELDS);
    Page<OutboxResponse> page = repository.findAll(spec, pageable).map(mapper::toResponse);
    return PaginatedResponse.from("Outbox events retrieved", page);
  }

  public Outbox requireById(UUID outboxId) {
    return repository
        .findById(outboxId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    ResponseCode.RESOURCE_NOT_FOUND, "Outbox event not found: " + outboxId));
  }
}
