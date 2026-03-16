--comment: seed default vehicle routing profiles required for routing and quote computation in MVP
INSERT INTO routing.vehicle_routing_profiles (
    created_at,
    created_by,
    vehicle_class,
    routing_engine,
    engine_costing_key,
    is_fallback_enabled,
    fallback_multiplier,
    is_active
) VALUES
      (CURRENT_TIMESTAMP, 'system', 'BICYCLE',   'VALHALLA', 'bicycle', TRUE, 1.1000, TRUE),
      (CURRENT_TIMESTAMP, 'system', 'E_BIKE',    'VALHALLA', 'bicycle', TRUE, 1.1000, TRUE),
      (CURRENT_TIMESTAMP, 'system', 'MOTORCYCLE','VALHALLA', 'auto',    TRUE, 1.1000, TRUE),
      (CURRENT_TIMESTAMP, 'system', 'CAR',       'VALHALLA', 'auto',    TRUE, 1.1000, TRUE),
      (CURRENT_TIMESTAMP, 'system', 'VAN',       'VALHALLA', 'auto',    TRUE, 1.1000, TRUE);