--comment: create a pricing plans table for scoped vehicle-specific pricing configuration with database-enforced invariants
CREATE TABLE routing.pricing_plans (
   id                    UUID          NOT NULL DEFAULT gen_random_uuid(),

   activated_at          TIMESTAMPTZ,
   activated_by          VARCHAR(36),
   archived_at           TIMESTAMPTZ,
   archived_by           VARCHAR(36),
   base_fare_amount      NUMERIC(19,4) NOT NULL,
   created_at            TIMESTAMPTZ   NOT NULL,
   created_by            VARCHAR(36)   NOT NULL,
   effective_from        TIMESTAMPTZ   NOT NULL,
   effective_to          TIMESTAMPTZ,
   maximum_charge_amount NUMERIC(19,4),
   minimum_charge_amount NUMERIC(19,4) NOT NULL,
   per_km_rate_amount    NUMERIC(19,4) NOT NULL,
   scope_id              UUID,
   scope_type            VARCHAR(20)   NOT NULL,
   status                VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
   updated_at            TIMESTAMPTZ,
   updated_by            VARCHAR(36),
   vehicle_class         VARCHAR(20)   NOT NULL,
   version               VARCHAR(100)  NOT NULL,

   CONSTRAINT pk_pricing_plans PRIMARY KEY (id),
   CONSTRAINT uq_pricing_plans_version UNIQUE (version)
);

--comment: add check constraints for pricing plans to enforce scope, lifecycle, and pricing invariants at the database level
ALTER TABLE routing.pricing_plans
    ADD CONSTRAINT chk_pricing_plans_activation_fields
        CHECK (
            (status <> 'ACTIVE')
                OR (activated_at IS NOT NULL AND activated_by IS NOT NULL)
            );

ALTER TABLE routing.pricing_plans
    ADD CONSTRAINT chk_pricing_plans_archive_after_activation
        CHECK (
            archived_at IS NULL
                OR activated_at IS NULL
                OR archived_at >= activated_at
            );

ALTER TABLE routing.pricing_plans
    ADD CONSTRAINT chk_pricing_plans_archive_fields
        CHECK (
            (status <> 'ARCHIVED')
                OR (archived_at IS NOT NULL AND archived_by IS NOT NULL)
            );

ALTER TABLE routing.pricing_plans
    ADD CONSTRAINT chk_pricing_plans_amounts_non_negative
        CHECK (
            base_fare_amount >= 0
                AND per_km_rate_amount >= 0
                AND minimum_charge_amount >= 0
                AND (maximum_charge_amount IS NULL OR maximum_charge_amount >= 0)
            );

ALTER TABLE routing.pricing_plans
    ADD CONSTRAINT chk_pricing_plans_effective_window
        CHECK (
            effective_to IS NULL
                OR effective_to > effective_from
            );

ALTER TABLE routing.pricing_plans
    ADD CONSTRAINT chk_pricing_plans_min_max_consistency
        CHECK (
            maximum_charge_amount IS NULL
                OR maximum_charge_amount >= minimum_charge_amount
            );

ALTER TABLE routing.pricing_plans
    ADD CONSTRAINT chk_pricing_plans_scope_consistency
        CHECK (
            (scope_type = 'GLOBAL' AND scope_id IS NULL)
                OR (scope_type IN ('ORGANIZATION', 'BRANCH') AND scope_id IS NOT NULL)
            );

ALTER TABLE routing.pricing_plans
    ADD CONSTRAINT chk_pricing_plans_scope_type
        CHECK (scope_type IN ('GLOBAL', 'ORGANIZATION', 'BRANCH'));

ALTER TABLE routing.pricing_plans
    ADD CONSTRAINT chk_pricing_plans_status
        CHECK (status IN ('DRAFT', 'ACTIVE', 'ARCHIVED'));

ALTER TABLE routing.pricing_plans
    ADD CONSTRAINT chk_pricing_plans_vehicle_class
        CHECK (vehicle_class IN ('BICYCLE', 'E_BIKE', 'MOTORCYCLE', 'CAR', 'VAN'));

--comment: create indexes for pricing plans
CREATE INDEX idx_pricing_plans_scope_vehicle
    ON routing.pricing_plans (scope_type, scope_id, vehicle_class);

CREATE INDEX idx_pricing_plans_status_window
    ON routing.pricing_plans (status, effective_from, effective_to);

CREATE UNIQUE INDEX uq_pricing_plans_active_scope_vehicle
    ON routing.pricing_plans (scope_type, scope_id, vehicle_class)
    WHERE status = 'ACTIVE';

--comment: add table and column comments for pricing plans
COMMENT ON TABLE routing.pricing_plans IS 'Scoped pricing configuration for quote calculation - resolved by branch, then organization, then global scope';
COMMENT ON COLUMN routing.pricing_plans.id IS 'Primary key - UUID identifier for the pricing plan record';
COMMENT ON COLUMN routing.pricing_plans.activated_at IS 'Timestamp when the plan was activated';
COMMENT ON COLUMN routing.pricing_plans.activated_by IS 'Actor identifier that activated the plan';
COMMENT ON COLUMN routing.pricing_plans.archived_at IS 'Timestamp when the plan was archived';
COMMENT ON COLUMN routing.pricing_plans.archived_by IS 'Actor identifier that archived the plan';
COMMENT ON COLUMN routing.pricing_plans.base_fare_amount IS 'Fixed starting charge applied before distance pricing';
COMMENT ON COLUMN routing.pricing_plans.created_at IS 'Timestamp when the pricing plan record was created';
COMMENT ON COLUMN routing.pricing_plans.created_by IS 'Actor identifier that created the pricing plan record';
COMMENT ON COLUMN routing.pricing_plans.effective_from IS 'Timestamp from which the plan becomes eligible for resolution';
COMMENT ON COLUMN routing.pricing_plans.effective_to IS 'Optional timestamp after which the plan is no longer eligible for resolution';
COMMENT ON COLUMN routing.pricing_plans.maximum_charge_amount IS 'Optional maximum payable amount cap after price computation';
COMMENT ON COLUMN routing.pricing_plans.minimum_charge_amount IS 'Minimum payable amount after price computation';
COMMENT ON COLUMN routing.pricing_plans.per_km_rate_amount IS 'Distance-based rate per kilometer used for quote calculation';
COMMENT ON COLUMN routing.pricing_plans.scope_id IS 'Identifier of the organization or branch when scope is not GLOBAL';
COMMENT ON COLUMN routing.pricing_plans.scope_type IS 'Pricing scope discriminator - GLOBAL, ORGANIZATION, or BRANCH';
COMMENT ON COLUMN routing.pricing_plans.status IS 'Lifecycle state of the pricing plan - DRAFT, ACTIVE, or ARCHIVED';
COMMENT ON COLUMN routing.pricing_plans.updated_at IS 'Timestamp of the most recent update to the pricing plan record';
COMMENT ON COLUMN routing.pricing_plans.updated_by IS 'Actor identifier that last updated the pricing plan record';
COMMENT ON COLUMN routing.pricing_plans.vehicle_class IS 'Business vehicle class this plan applies to';
COMMENT ON COLUMN routing.pricing_plans.version IS 'Immutable business version identifier captured into quote snapshots';