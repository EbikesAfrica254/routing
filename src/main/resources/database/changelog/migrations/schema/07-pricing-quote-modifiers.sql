--comment: create the pricing quote modifiers table for immutable per-modifier quote breakdown snapshots
CREATE TABLE routing.pricing_quote_modifiers (
     id                  UUID          NOT NULL DEFAULT gen_random_uuid(),
     pricing_quote_id    UUID          NOT NULL,
     pricing_modifier_id UUID          NOT NULL,

     modifier_version    VARCHAR(100)  NOT NULL,
     modifier_type       VARCHAR(40)   NOT NULL,
     stacking_mode       VARCHAR(20)   NOT NULL,

     priority            INTEGER       NOT NULL,
     value_amount        NUMERIC(19,4) NOT NULL,
     applied_amount      NUMERIC(19,4) NOT NULL,
     currency            VARCHAR(3)       NOT NULL,

     condition_snapshot  JSONB         NOT NULL,
     created_at          TIMESTAMPTZ   NOT NULL,

     CONSTRAINT pk_pricing_quote_modifiers PRIMARY KEY (id),
     CONSTRAINT fk_pricing_quote_modifiers_quote
         FOREIGN KEY (pricing_quote_id)
             REFERENCES routing.pricing_quotes (id),
     CONSTRAINT fk_pricing_quote_modifiers_modifier
         FOREIGN KEY (pricing_modifier_id)
             REFERENCES routing.pricing_modifiers (id)
);

--comment: add check constraints for pricing quote modifiers to enforce immutable snapshot semantics
ALTER TABLE routing.pricing_quote_modifiers
    ADD CONSTRAINT chk_pricing_quote_modifiers_modifier_type
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

ALTER TABLE routing.pricing_quote_modifiers
    ADD CONSTRAINT chk_pricing_quote_modifiers_stacking_mode
        CHECK (stacking_mode IN ('STACKABLE', 'EXCLUSIVE'));

ALTER TABLE routing.pricing_quote_modifiers
    ADD CONSTRAINT chk_pricing_quote_modifiers_currency
        CHECK (currency = 'KES');

ALTER TABLE routing.pricing_quote_modifiers
    ADD CONSTRAINT chk_pricing_quote_modifiers_priority_non_negative
        CHECK (priority >= 0);

ALTER TABLE routing.pricing_quote_modifiers
    ADD CONSTRAINT chk_pricing_quote_modifiers_value_non_negative
        CHECK (value_amount >= 0);

ALTER TABLE routing.pricing_quote_modifiers
    ADD CONSTRAINT chk_pricing_quote_modifiers_applied_amount_semantics
        CHECK (
            (
                modifier_type IN ('FIXED_SURCHARGE', 'PERCENT_SURCHARGE', 'PEAK_WINDOW_SURCHARGE', 'ORDER_TYPE_ADJUSTMENT', 'MIN_PRICE_FLOOR')
                    AND applied_amount >= 0
                )
                OR
            (
                modifier_type IN ('FIXED_DISCOUNT', 'PERCENT_DISCOUNT', 'MAX_PRICE_CAP')
                    AND applied_amount <= 0
                )
            );

--comment: create indexes for pricing quote modifiers
CREATE INDEX idx_pricing_quote_modifiers_quote_id
    ON routing.pricing_quote_modifiers (pricing_quote_id);

CREATE INDEX idx_pricing_quote_modifiers_modifier_id
    ON routing.pricing_quote_modifiers (pricing_modifier_id);

CREATE INDEX idx_pricing_quote_modifiers_quote_priority
    ON routing.pricing_quote_modifiers (pricing_quote_id, priority);

CREATE UNIQUE INDEX uq_pricing_quote_modifiers_quote_modifier
    ON routing.pricing_quote_modifiers (pricing_quote_id, pricing_modifier_id);

--comment: add table and column comments for pricing quote modifiers
COMMENT ON TABLE routing.pricing_quote_modifiers IS 'Immutable breakdown of modifiers actually applied to a quote, preserving auditability and reproducibility';
COMMENT ON COLUMN routing.pricing_quote_modifiers.id IS 'Primary key - UUID identifier for the quote modifier snapshot row';
COMMENT ON COLUMN routing.pricing_quote_modifiers.pricing_quote_id IS 'Referenced pricing quote that this applied modifier belongs to';
COMMENT ON COLUMN routing.pricing_quote_modifiers.pricing_modifier_id IS 'Referenced source pricing modifier from which this snapshot row was produced';
COMMENT ON COLUMN routing.pricing_quote_modifiers.modifier_version IS 'Immutable business version of the source modifier captured at quote time';
COMMENT ON COLUMN routing.pricing_quote_modifiers.modifier_type IS 'Type of pricing adjustment applied by this modifier snapshot';
COMMENT ON COLUMN routing.pricing_quote_modifiers.stacking_mode IS 'Stacking behavior captured from the source modifier at quote time';
COMMENT ON COLUMN routing.pricing_quote_modifiers.priority IS 'Deterministic evaluation order of the modifier within the quote computation';
COMMENT ON COLUMN routing.pricing_quote_modifiers.value_amount IS 'Raw configured modifier value captured from the source modifier';
COMMENT ON COLUMN routing.pricing_quote_modifiers.applied_amount IS 'Net monetary effect this modifier had on the final quote amount';
COMMENT ON COLUMN routing.pricing_quote_modifiers.currency IS 'Currency for the stored modifier amounts - fixed to KES for MVP';
COMMENT ON COLUMN routing.pricing_quote_modifiers.condition_snapshot IS 'Immutable JSON snapshot of the evaluated modifier conditions at quote time';
COMMENT ON COLUMN routing.pricing_quote_modifiers.created_at IS 'Timestamp when the quote modifier snapshot row was created';