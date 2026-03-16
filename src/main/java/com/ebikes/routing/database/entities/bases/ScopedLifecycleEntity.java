package com.ebikes.routing.database.entities.bases;

import java.io.Serial;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;

import com.ebikes.routing.enums.PricingStatus;
import com.ebikes.routing.enums.ResponseCode;
import com.ebikes.routing.enums.ScopeType;
import com.ebikes.routing.exceptions.ValidationException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ScopedLifecycleEntity extends AuditableEntity {

  @Serial private static final long serialVersionUID = 1L;

  @Column(name = "activated_at", columnDefinition = "TIMESTAMPTZ")
  protected OffsetDateTime activatedAt;

  @Column(name = "activated_by", length = 36)
  protected String activatedBy;

  @Column(name = "archived_at", columnDefinition = "TIMESTAMPTZ")
  protected OffsetDateTime archivedAt;

  @Column(name = "archived_by", length = 36)
  protected String archivedBy;

  @Column(name = "scope_id")
  protected UUID scopeId;

  @Column(name = "scope_type", nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  protected ScopeType scopeType;

  @Column(name = "status", nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  protected PricingStatus status;

  protected void activateLifecycle(
      String activatedBy, OffsetDateTime activatedAt, String entityName) {
    if (this.status != PricingStatus.DRAFT) {
      throw new ValidationException(
          ResponseCode.INVALID_STATE,
          "Only DRAFT " + entityName + " can be activated. Current status: " + this.status,
          "status",
          this.status);
    }

    validateActor(activatedBy, "Activated by is required");
    validateTimestamp(activatedAt, "Activated at is required");

    this.activatedAt = activatedAt;
    this.activatedBy = activatedBy.trim();
    this.archivedAt = null;
    this.archivedBy = null;
    this.status = PricingStatus.ACTIVE;
  }

  protected void archiveLifecycle(String archivedBy, OffsetDateTime archivedAt, String entityName) {
    if (this.status != PricingStatus.ACTIVE) {
      throw new ValidationException(
          ResponseCode.INVALID_STATE,
          "Only ACTIVE " + entityName + " can be archived. Current status: " + this.status,
          "status",
          this.status);
    }

    validateActor(archivedBy, "Archived by is required");
    validateTimestamp(archivedAt, "Archived at is required");

    if (this.activatedAt != null && archivedAt.isBefore(this.activatedAt)) {
      throw new IllegalArgumentException(
          "Archived at must be greater than or equal to activated at");
    }

    this.archivedAt = archivedAt;
    this.archivedBy = archivedBy.trim();
    this.status = PricingStatus.ARCHIVED;
  }

  protected void ensureDraft(String entityName) {
    if (this.status != PricingStatus.DRAFT) {
      throw new ValidationException(
          ResponseCode.INVALID_STATE,
          "Only DRAFT " + entityName + " can be modified. Current status: " + this.status,
          "status",
          this.status);
    }
  }

  protected void initializeScope(UUID scopeId, ScopeType scopeType) {
    validateScope(scopeType, scopeId);
    this.scopeId = scopeId;
    this.scopeType = scopeType;
  }

  protected void initializeScopedLifecycle(
      UUID scopeId,
      ScopeType scopeType,
      PricingStatus status,
      OffsetDateTime activatedAt,
      String activatedBy,
      OffsetDateTime archivedAt,
      String archivedBy) {
    this.activatedAt = activatedAt;
    this.activatedBy = activatedBy;
    this.archivedAt = archivedAt;
    this.archivedBy = archivedBy;

    initializeScope(scopeId, scopeType);
    initializeStatus(status);
    validateLifecycleFields();
  }

  protected void initializeStatus(PricingStatus status) {
    this.status = status == null ? PricingStatus.DRAFT : status;
  }

  protected void updateScope(UUID scopeId, ScopeType scopeType) {
    initializeScope(scopeId, scopeType);
  }

  protected void validateLifecycleFields() {
    if (this.status == PricingStatus.ACTIVE
        && (this.activatedAt == null || this.activatedBy == null || this.activatedBy.isBlank())) {
      throw new IllegalArgumentException(
          "Activated at and activated by are required when status is ACTIVE");
    }

    if (this.status == PricingStatus.ARCHIVED
        && (this.archivedAt == null || this.archivedBy == null || this.archivedBy.isBlank())) {
      throw new IllegalArgumentException(
          "Archived at and archived by are required when status is ARCHIVED");
    }

    if (this.archivedAt != null
        && this.activatedAt != null
        && this.archivedAt.isBefore(this.activatedAt)) {
      throw new IllegalArgumentException(
          "Archived at must be greater than or equal to activated at");
    }
  }

  protected void validateScope(ScopeType scopeType, UUID scopeId) {
    if (scopeType == null) {
      throw new IllegalArgumentException("Scope type is required");
    }

    if (scopeType == ScopeType.GLOBAL && scopeId != null) {
      throw new IllegalArgumentException("Scope id must be null when scope type is GLOBAL");
    }

    if (scopeType != ScopeType.GLOBAL && scopeId == null) {
      throw new IllegalArgumentException(
          "Scope id is required when scope type is ORGANIZATION or BRANCH");
    }
  }

  private void validateActor(String actor, String message) {
    if (actor == null || actor.isBlank()) {
      throw new IllegalArgumentException(message);
    }
  }

  private void validateTimestamp(OffsetDateTime timestamp, String message) {
    if (timestamp == null) {
      throw new IllegalArgumentException(message);
    }
  }
}
