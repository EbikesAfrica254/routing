--comment: create a vehicle routing profiles table for controlled business-vehicle-to-engine-profile mapping
CREATE TABLE routing.vehicle_routing_profiles (
  id                  UUID         NOT NULL DEFAULT gen_random_uuid(),
  created_at          TIMESTAMPTZ  NOT NULL,
  created_by          VARCHAR(36)  NOT NULL,
  updated_at          TIMESTAMPTZ,
  updated_by          VARCHAR(36),

  vehicle_class       VARCHAR(20)  NOT NULL,
  routing_engine      VARCHAR(20)  NOT NULL,
  engine_costing_key  VARCHAR(50)  NOT NULL,
  is_fallback_enabled BOOLEAN      NOT NULL DEFAULT TRUE,
  fallback_multiplier NUMERIC(8,4),
  is_active           BOOLEAN      NOT NULL DEFAULT TRUE,

  CONSTRAINT pk_vehicle_routing_profiles PRIMARY KEY (id),
  CONSTRAINT uq_vehicle_routing_profiles_vehicle_class UNIQUE (vehicle_class)
);

--comment: add check constraints for vehicle routing profiles to enforce controlled configuration at the database level
ALTER TABLE routing.vehicle_routing_profiles
    ADD CONSTRAINT chk_vehicle_routing_profiles_vehicle_class
        CHECK (vehicle_class IN ('BICYCLE', 'E_BIKE', 'MOTORCYCLE', 'CAR', 'VAN'));

ALTER TABLE routing.vehicle_routing_profiles
    ADD CONSTRAINT chk_vehicle_routing_profiles_routing_engine
        CHECK (routing_engine IN ('VALHALLA', 'HAVERSINE_FALLBACK'));

ALTER TABLE routing.vehicle_routing_profiles
    ADD CONSTRAINT chk_vehicle_routing_profiles_engine_costing_key
        CHECK (engine_costing_key IN ('bicycle', 'auto'));

ALTER TABLE routing.vehicle_routing_profiles
    ADD CONSTRAINT chk_vehicle_routing_profiles_fallback_multiplier
        CHECK (
            fallback_multiplier IS NULL
                OR fallback_multiplier >= 1.0000
            );

ALTER TABLE routing.vehicle_routing_profiles
    ADD CONSTRAINT chk_vehicle_routing_profiles_fallback_consistency
        CHECK (
            (is_fallback_enabled = TRUE AND fallback_multiplier IS NOT NULL)
                OR (is_fallback_enabled = FALSE)
            );

--comment: create indexes for vehicle routing profiles
CREATE INDEX idx_vehicle_routing_profiles_engine_active
    ON routing.vehicle_routing_profiles (routing_engine, is_active);

--comment: add table and column comments for vehicle routing profiles
COMMENT ON TABLE routing.vehicle_routing_profiles IS 'Controlled mapping from business vehicle classes to routing-engine profiles and fallback behavior';
COMMENT ON COLUMN routing.vehicle_routing_profiles.id IS 'Primary key - UUID identifier for the routing profile record';
COMMENT ON COLUMN routing.vehicle_routing_profiles.created_at IS 'Timestamp when the routing profile record was created';
COMMENT ON COLUMN routing.vehicle_routing_profiles.created_by IS 'Actor identifier that created the routing profile record';
COMMENT ON COLUMN routing.vehicle_routing_profiles.updated_at IS 'Timestamp of the most recent update to the routing profile record';
COMMENT ON COLUMN routing.vehicle_routing_profiles.updated_by IS 'Actor identifier that last updated the routing profile record';
COMMENT ON COLUMN routing.vehicle_routing_profiles.vehicle_class IS 'Business vehicle class - one durable profile per class for MVP';
COMMENT ON COLUMN routing.vehicle_routing_profiles.routing_engine IS 'Routing engine used for this vehicle class - Valhalla or haversine fallback mode';
COMMENT ON COLUMN routing.vehicle_routing_profiles.engine_costing_key IS 'Engine-specific costing/profile key used by the routing adapter - bicycle or auto for MVP';
COMMENT ON COLUMN routing.vehicle_routing_profiles.is_fallback_enabled IS 'Whether fallback distance computation is allowed when the primary routing engine is unavailable';
COMMENT ON COLUMN routing.vehicle_routing_profiles.fallback_multiplier IS 'Safety multiplier applied to fallback distance computation to avoid optimistic underpricing';
COMMENT ON COLUMN routing.vehicle_routing_profiles.is_active IS 'Whether this routing profile is currently active and eligible for resolution';