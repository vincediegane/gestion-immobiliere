CREATE TABLE owners (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    full_name VARCHAR(200) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(320),
    address VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT ck_owners_full_name_not_blank CHECK (BTRIM(full_name) <> ''),
    CONSTRAINT ck_owners_phone_not_blank CHECK (BTRIM(phone) <> '')
);

CREATE UNIQUE INDEX uq_owners_organization_email_active
    ON owners (organization_id, LOWER(email))
    WHERE email IS NOT NULL AND deleted_at IS NULL;

CREATE INDEX idx_owners_organization_active_name
    ON owners (organization_id, full_name)
    WHERE deleted_at IS NULL;
