ALTER TABLE owners
    ADD CONSTRAINT uq_owners_id_organization UNIQUE (id, organization_id);

CREATE TABLE properties (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    owner_id UUID NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    address VARCHAR(500) NOT NULL,
    city VARCHAR(120) NOT NULL,
    type VARCHAR(40) NOT NULL,
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_properties_owner_tenant
        FOREIGN KEY (owner_id, organization_id)
        REFERENCES owners (id, organization_id),
    CONSTRAINT ck_properties_name_not_blank CHECK (BTRIM(name) <> ''),
    CONSTRAINT ck_properties_address_not_blank CHECK (BTRIM(address) <> ''),
    CONSTRAINT ck_properties_city_not_blank CHECK (BTRIM(city) <> ''),
    CONSTRAINT ck_properties_type CHECK (type IN (
        'APARTMENT_BUILDING', 'HOUSE', 'VILLA', 'COMMERCIAL', 'LAND'
    )),
    CONSTRAINT ck_properties_status CHECK (status IN (
        'AVAILABLE', 'OCCUPIED', 'MAINTENANCE'
    ))
);

CREATE INDEX idx_properties_organization_active_name
    ON properties (organization_id, name)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_properties_organization_owner_active
    ON properties (organization_id, owner_id)
    WHERE deleted_at IS NULL;
