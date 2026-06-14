ALTER TABLE payments
    ADD COLUMN idempotency_key VARCHAR(100);

UPDATE payments
SET idempotency_key = id::text
WHERE idempotency_key IS NULL;

ALTER TABLE payments
    ALTER COLUMN idempotency_key SET NOT NULL;

CREATE UNIQUE INDEX uq_payments_organization_idempotency
    ON payments (organization_id, idempotency_key);

CREATE UNIQUE INDEX uq_leases_active_unit
    ON leases (organization_id, unit_id)
    WHERE status = 'ACTIVE';
