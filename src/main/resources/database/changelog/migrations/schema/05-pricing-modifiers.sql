--comment: create the pricing modifiers table for scoped conditional pricing adjustments applied during quote calculation
CREATE TABLE routing.pricing_modifiers (
   id                    UUID          NOT NULL DEFAULT gen_random_uuid(),

   activated_at          TIMESTAMPTZ,
   activated_by          VARCHAR(36),
   applies_to_order_type VARCHAR(20),
   archived_at           TIMESTAMPTZ,
   archived_by           VARCHAR(36),
   condition_set         JSONB,
   created_at            TIMESTAMPTZ   NOT NULL,
   created_by            VARCHAR(36)   NOT NULL,
   effective_from        TIMESTAMPTZ   NOT NULL,
   effective_to          TIMESTAMPTZ,
   modifier_type         VARCHAR(50)   NOT NULL,
   priority              INTEGER       NOT NULL,
   scope_id              UUID,
   scope_type            VARCHAR(20)   NOT NULL,
   stacking_mode         VARCHAR(20)   NOT NULL,
   status                VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
   updated_at            TIMESTAMPTZ,
   updated_by            VARCHAR(36),
   value_amount          NUMERIC(19,4) NOT NULL,
   vehicle_class         VARCHAR(20),
   version               VARCHAR(100)  NOT NULL,

   CONSTRAINT pk_pricing_modifiers PRIMARY KEY (id),
   CONSTRAINT uq_pricing_modifiers_version UNIQUE (version)
);

--comment: add check constraints for pricing modifiers to enforce scope, lifecycle and modifier semantics
ALTER TABLE routing.pricing_modifiers
    ADD CONSTRAINT chk_pricing_modifiers_activation_fields
        CHECK (
            (status <> 'ACTIVE')
                OR (activated_at IS NOT NULL AND activated_by IS NOT NULL)
            );

ALTER TABLE routing.pricing_modifiers
    ADD CONSTRAINT chk_pricing_modifiers_applies_to_order_type
        CHECK (
            applies_to_order_type IS NULL
                OR applies_to_order_type IN ('EXPLICIT')
            );

ALTER TABLE routing.pricing_modifiers
    ADD CONSTRAINT chk_pricing_modifiers_archive_fields
        CHECK (
            (status <> 'ARCHIVED')
                OR (archived_at IS NOT NULL AND archived_by IS NOT NULL)
            );

ALTER TABLE routing.pricing_modifiers
    ADD CONSTRAINT chk_pricing_modifiers_effective_window
        CHECK (
            effective_to IS NULL
                OR effective_to > effective_from
            );

ALTER TABLE routing.pricing_modifiers
    ADD CONSTRAINT chk_pricing_modifiers_modifier_type
        CHECK (
            modifier_type IN (
                              'FIXED_SURCHARGE',
                              'FIXED_DISCOUNT',
                              'PERCENT_SURCHARGE',
                              'PERCENT_DISCOUNT',
                              'MIN_PRICE_FLOOR',
                              'MAX_PRICE_CAP',
                              'PEAK_WINDOW_SURCHARGE',
                              'ORDER_TYPE_SURCHARGE',
                              'ORDER_TYPE_DISCOUNT'
                )
            );

ALTER TABLE routing.pricing_modifiers
    ADD CONSTRAINT chk_pricing_modifiers_priority_non_negative
        CHECK (priority >= 0);

ALTER TABLE routing.pricing_modifiers
    ADD CONSTRAINT chk_pricing_modifiers_scope_consistency
        CHECK (
            (scope_type = 'GLOBAL' AND scope_id IS NULL)
                OR (scope_type IN ('ORGANIZATION', 'BRANCH') AND scope_id IS NOT NULL)
            );

ALTER TABLE routing.pricing_modifiers
    ADD CONSTRAINT chk_pricing_modifiers_scope_type
        CHECK (scope_type IN ('GLOBAL', 'ORGANIZATION', 'BRANCH'));

ALTER TABLE routing.pricing_modifiers
    ADD CONSTRAINT chk_pricing_modifiers_stacking_mode
        CHECK (stacking_mode IN ('STACKABLE', 'EXCLUSIVE'));

ALTER TABLE routing.pricing_modifiers
    ADD CONSTRAINT chk_pricing_modifiers_value_non_negative
        CHECK (value_amount >= 0);

ALTER TABLE routing.pricing_modifiers
    ADD CONSTRAINT chk_pricing_modifiers_vehicle_class
        CHECK (
            vehicle_class IS NULL
                OR vehicle_class IN ('BICYCLE', 'E_BIKE', 'MOTORCYCLE', 'CAR', 'VAN')
            );

--comment: create indexes for pricing modifiers
CREATE INDEX idx_pricing_modifiers_scope_match
    ON routing.pricing_modifiers (scope_type, scope_id, vehicle_class, applies_to_order_type);

CREATE INDEX idx_pricing_modifiers_status_window
    ON routing.pricing_modifiers (status, effective_from, effective_to);

CREATE INDEX idx_pricing_modifiers_priority
    ON routing.pricing_modifiers (scope_type, scope_id, priority, status);

CREATE UNIQUE INDEX uq_pricing_modifiers_active_scope_priority
    ON routing.pricing_modifiers (scope_type, scope_id, priority)
    WHERE status = 'ACTIVE';

--comment: add table and column comments for pricing modifiers
COMMENT ON TABLE routing.pricing_modifiers IS 'Conditional pricing adjustments evaluated during quote calculation and applied according to priority order';
COMMENT ON COLUMN routing.pricing_modifiers.id IS 'Primary key - UUID identifier for the pricing modifier';
COMMENT ON COLUMN routing.pricing_modifiers.activated_at IS 'Timestamp when the modifier was activated';
COMMENT ON COLUMN routing.pricing_modifiers.activated_by IS 'Actor identifier that activated the modifier';
COMMENT ON COLUMN routing.pricing_modifiers.applies_to_order_type IS 'Optional order type filter for modifier applicability';
COMMENT ON COLUMN routing.pricing_modifiers.archived_at IS 'Timestamp when the modifier was archived';
COMMENT ON COLUMN routing.pricing_modifiers.archived_by IS 'Actor identifier that archived the modifier';
COMMENT ON COLUMN routing.pricing_modifiers.condition_set IS 'JSONB document describing conditions under which the modifier becomes applicable';
COMMENT ON COLUMN routing.pricing_modifiers.created_at IS 'Timestamp when the modifier record was created';
COMMENT ON COLUMN routing.pricing_modifiers.created_by IS 'Actor identifier that created the modifier';
COMMENT ON COLUMN routing.pricing_modifiers.effective_from IS 'Timestamp from which the modifier becomes eligible for evaluation';
COMMENT ON COLUMN routing.pricing_modifiers.effective_to IS 'Optional timestamp after which the modifier is no longer eligible';
COMMENT ON COLUMN routing.pricing_modifiers.modifier_type IS 'Type of pricing adjustment performed by the modifier';
COMMENT ON COLUMN routing.pricing_modifiers.priority IS 'Deterministic evaluation order for modifiers - lower values evaluated first';
COMMENT ON COLUMN routing.pricing_modifiers.scope_id IS 'Identifier of organization or branch when scope is not GLOBAL';
COMMENT ON COLUMN routing.pricing_modifiers.scope_type IS 'Modifier scope discriminator - GLOBAL, ORGANIZATION, or BRANCH';
COMMENT ON COLUMN routing.pricing_modifiers.stacking_mode IS 'Controls whether the modifier stacks with others or is exclusive';
COMMENT ON COLUMN routing.pricing_modifiers.status IS 'Lifecycle state of the modifier - DRAFT, ACTIVE, or ARCHIVED';
COMMENT ON COLUMN routing.pricing_modifiers.updated_at IS 'Timestamp of the most recent update to the modifier';
COMMENT ON COLUMN routing.pricing_modifiers.updated_by IS 'Actor identifier that last updated the modifier';
COMMENT ON COLUMN routing.pricing_modifiers.value_amount IS 'Magnitude of the pricing adjustment applied by the modifier';
COMMENT ON COLUMN routing.pricing_modifiers.vehicle_class IS 'Optional vehicle class filter for modifier applicability';
COMMENT ON COLUMN routing.pricing_modifiers.version IS 'Immutable business version identifier captured into quote snapshots';