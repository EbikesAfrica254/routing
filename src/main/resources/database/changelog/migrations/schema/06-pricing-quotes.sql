--comment: create the pricing quotes table for immutable quote snapshots with database-enforced pricing and routing invariants
CREATE TABLE routing.pricing_quotes (
    id                     UUID          NOT NULL DEFAULT gen_random_uuid(),

    base_fare_amount       NUMERIC(19,4) NOT NULL,
    branch_id              UUID,
    currency               VARCHAR(3)    NOT NULL,
    created_at             TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    destination_latitude   NUMERIC(9,6)  NOT NULL,
    destination_longitude  NUMERIC(9,6)  NOT NULL,
    distance_charge_amount NUMERIC(19,4) NOT NULL,
    distance_meters        INTEGER       NOT NULL,
    duration_seconds       INTEGER       NOT NULL,
    engine_costing_used    VARCHAR(50)   NOT NULL,
    expires_at             TIMESTAMPTZ,
    final_amount           NUMERIC(19,4) NOT NULL,
    is_committed           BOOLEAN       NOT NULL DEFAULT FALSE,
    is_fallback            BOOLEAN       NOT NULL DEFAULT FALSE,
    modifiers_total_amount NUMERIC(19,4) NOT NULL DEFAULT 0,
    order_id               UUID,
    order_type             VARCHAR(20)   NOT NULL,
    organization_id        UUID,
    origin_latitude        NUMERIC(9,6)  NOT NULL,
    origin_longitude       NUMERIC(9,6)  NOT NULL,
    pricing_plan_id        UUID          NOT NULL,
    pricing_plan_version   VARCHAR(100)  NOT NULL,
    route_computation_mode VARCHAR(20)   NOT NULL,
    routing_engine         VARCHAR(20)   NOT NULL,
    status                 VARCHAR(20)   NOT NULL DEFAULT 'CREATED',
    vehicle_class          VARCHAR(20)   NOT NULL,

    CONSTRAINT pk_pricing_quotes PRIMARY KEY (id),
    CONSTRAINT fk_pricing_quotes_pricing_plan
        FOREIGN KEY (pricing_plan_id)
            REFERENCES routing.pricing_plans (id)
);

--comment: add check constraints for pricing quotes to enforce immutable snapshot and routing invariants
ALTER TABLE routing.pricing_quotes
    ADD CONSTRAINT chk_pricing_quotes_amounts_non_negative
        CHECK (
            base_fare_amount >= 0
                AND distance_charge_amount >= 0
                AND final_amount >= 0
            );

ALTER TABLE routing.pricing_quotes
    ADD CONSTRAINT chk_pricing_quotes_branch_org_consistency
        CHECK (
            branch_id IS NULL
                OR organization_id IS NOT NULL
            );

ALTER TABLE routing.pricing_quotes
    ADD CONSTRAINT chk_pricing_quotes_commit_expiry_consistency
        CHECK (
            (is_committed = TRUE AND expires_at IS NULL)
                OR (is_committed = FALSE AND expires_at IS NOT NULL AND expires_at > created_at)
            );

ALTER TABLE routing.pricing_quotes
    ADD CONSTRAINT chk_pricing_quotes_currency
        CHECK (currency = 'KES');

ALTER TABLE routing.pricing_quotes
    ADD CONSTRAINT chk_pricing_quotes_destination_latitude
        CHECK (destination_latitude BETWEEN -90 AND 90);

ALTER TABLE routing.pricing_quotes
    ADD CONSTRAINT chk_pricing_quotes_destination_longitude
        CHECK (destination_longitude BETWEEN -180 AND 180);

ALTER TABLE routing.pricing_quotes
    ADD CONSTRAINT chk_pricing_quotes_distance_meters
        CHECK (distance_meters >= 0);

ALTER TABLE routing.pricing_quotes
    ADD CONSTRAINT chk_pricing_quotes_duration_seconds
        CHECK (duration_seconds >= 0);

ALTER TABLE routing.pricing_quotes
    ADD CONSTRAINT chk_pricing_quotes_fallback_consistency
        CHECK (
            (is_fallback = TRUE AND route_computation_mode = 'FALLBACK' AND routing_engine = 'HAVERSINE_FALLBACK')
                OR
            (is_fallback = FALSE AND route_computation_mode = 'ROUTED' AND routing_engine = 'VALHALLA')
            );

ALTER TABLE routing.pricing_quotes
    ADD CONSTRAINT chk_pricing_quotes_final_amount_consistency
        CHECK (
            final_amount = base_fare_amount + distance_charge_amount + modifiers_total_amount
            );

ALTER TABLE routing.pricing_quotes
    ADD CONSTRAINT chk_pricing_quotes_order_type
        CHECK (order_type IN ('EXPLICIT'));

ALTER TABLE routing.pricing_quotes
    ADD CONSTRAINT chk_pricing_quotes_origin_latitude
        CHECK (origin_latitude BETWEEN -90 AND 90);

ALTER TABLE routing.pricing_quotes
    ADD CONSTRAINT chk_pricing_quotes_origin_longitude
        CHECK (origin_longitude BETWEEN -180 AND 180);

ALTER TABLE routing.pricing_quotes
    ADD CONSTRAINT chk_pricing_quotes_route_computation_mode
        CHECK (route_computation_mode IN ('ROUTED', 'FALLBACK'));

ALTER TABLE routing.pricing_quotes
    ADD CONSTRAINT chk_pricing_quotes_routing_engine
        CHECK (routing_engine IN ('VALHALLA', 'HAVERSINE_FALLBACK'));

ALTER TABLE routing.pricing_quotes
    ADD CONSTRAINT chk_pricing_quotes_status
        CHECK (status IN ('CREATED', 'APPLIED', 'EXPIRED', 'SUPERSEDED'));

ALTER TABLE routing.pricing_quotes
    ADD CONSTRAINT chk_pricing_quotes_status_expiry_consistency
        CHECK (
            (status = 'CREATED')
                OR (status = 'APPLIED' AND is_committed = TRUE)
                OR (status = 'EXPIRED' AND is_committed = FALSE)
                OR (status = 'SUPERSEDED')
            );

ALTER TABLE routing.pricing_quotes
    ADD CONSTRAINT chk_pricing_quotes_vehicle_class
        CHECK (vehicle_class IN ('BICYCLE', 'E_BIKE', 'MOTORCYCLE', 'CAR', 'VAN'));

--comment: create indexes for pricing quotes
CREATE INDEX idx_pricing_quotes_order_id
    ON routing.pricing_quotes (order_id);

CREATE INDEX idx_pricing_quotes_pricing_plan_id
    ON routing.pricing_quotes (pricing_plan_id);

CREATE INDEX idx_pricing_quotes_scope_time
    ON routing.pricing_quotes (organization_id, branch_id, created_at);

CREATE INDEX idx_pricing_quotes_status_time
    ON routing.pricing_quotes (status, created_at);

CREATE INDEX idx_pricing_quotes_vehicle_time
    ON routing.pricing_quotes (vehicle_class, created_at);

CREATE UNIQUE INDEX uq_pricing_quotes_committed_order
    ON routing.pricing_quotes (order_id)
    WHERE is_committed = TRUE AND order_id IS NOT NULL;

--comment: add table and column comments for pricing quotes
COMMENT ON TABLE routing.pricing_quotes IS 'Immutable commercial quote snapshot storing the routed distance, duration, resolved plan version and final computed price';
COMMENT ON COLUMN routing.pricing_quotes.id IS 'Primary key - UUID identifier for the pricing quote';
COMMENT ON COLUMN routing.pricing_quotes.base_fare_amount IS 'Base fare component captured from the resolved pricing plan';
COMMENT ON COLUMN routing.pricing_quotes.branch_id IS 'External branch identifier used for plan resolution and audit context';
COMMENT ON COLUMN routing.pricing_quotes.created_at IS 'Timestamp when the pricing quote record was created';
COMMENT ON COLUMN routing.pricing_quotes.currency IS 'Commercial currency for all quote amounts - fixed to KES for MVP';
COMMENT ON COLUMN routing.pricing_quotes.destination_latitude IS 'Destination latitude captured into the immutable quote snapshot';
COMMENT ON COLUMN routing.pricing_quotes.destination_longitude IS 'Destination longitude captured into the immutable quote snapshot';
COMMENT ON COLUMN routing.pricing_quotes.distance_charge_amount IS 'Distance charge component calculated from routed distance and plan rate';
COMMENT ON COLUMN routing.pricing_quotes.distance_meters IS 'Distance returned by routing or fallback computation in meters';
COMMENT ON COLUMN routing.pricing_quotes.duration_seconds IS 'Duration returned by routing or fallback computation in seconds';
COMMENT ON COLUMN routing.pricing_quotes.engine_costing_used IS 'Engine-specific costing/profile key used during routing';
COMMENT ON COLUMN routing.pricing_quotes.expires_at IS 'Expiry timestamp for non-committed quotes - null for committed quotes';
COMMENT ON COLUMN routing.pricing_quotes.final_amount IS 'Final payable amount after base fare, distance charge and modifiers';
COMMENT ON COLUMN routing.pricing_quotes.is_committed IS 'Whether this quote is the committed commercial quote for the order';
COMMENT ON COLUMN routing.pricing_quotes.is_fallback IS 'Whether the quote was computed using fallback distance logic instead of primary routing';
COMMENT ON COLUMN routing.pricing_quotes.modifiers_total_amount IS 'Net total effect of all applied modifiers, positive or negative';
COMMENT ON COLUMN routing.pricing_quotes.order_id IS 'External order identifier if the quote is associated with an order';
COMMENT ON COLUMN routing.pricing_quotes.order_type IS 'Order type used during modifier applicability evaluation';
COMMENT ON COLUMN routing.pricing_quotes.organization_id IS 'External organization identifier used for plan resolution and audit context';
COMMENT ON COLUMN routing.pricing_quotes.origin_latitude IS 'Origin latitude captured into the immutable quote snapshot';
COMMENT ON COLUMN routing.pricing_quotes.origin_longitude IS 'Origin longitude captured into the immutable quote snapshot';
COMMENT ON COLUMN routing.pricing_quotes.pricing_plan_id IS 'Referenced pricing plan used to produce the quote';
COMMENT ON COLUMN routing.pricing_quotes.pricing_plan_version IS 'Immutable pricing plan version captured into the quote snapshot';
COMMENT ON COLUMN routing.pricing_quotes.route_computation_mode IS 'Whether the quote used a routed result or explicit fallback computation';
COMMENT ON COLUMN routing.pricing_quotes.routing_engine IS 'Routing engine that produced the route used for this quote';
COMMENT ON COLUMN routing.pricing_quotes.status IS 'Lifecycle state of the quote snapshot - CREATED, APPLIED, EXPIRED or SUPERSEDED';
COMMENT ON COLUMN routing.pricing_quotes.vehicle_class IS 'Business vehicle class used to resolve routing profile and pricing plan';