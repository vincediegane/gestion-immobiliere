CREATE TABLE organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_organizations_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

INSERT INTO organizations (id, name, status, created_at, updated_at)
VALUES ('00000000-0000-0000-0000-000000000001', 'Agence Demo Senegal', 'ACTIVE', NOW(), NOW());

CREATE TABLE app_users (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    full_name VARCHAR(200) NOT NULL,
    role VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_app_users_organization UNIQUE (id, organization_id),
    CONSTRAINT ck_app_users_role CHECK (role IN ('ADMIN', 'GESTIONNAIRE')),
    CONSTRAINT ck_app_users_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);
CREATE UNIQUE INDEX uq_app_users_email ON app_users (LOWER(email));

ALTER TABLE properties
    ADD CONSTRAINT uq_properties_id_organization UNIQUE (id, organization_id);

CREATE TABLE units (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    property_id UUID NOT NULL,
    name VARCHAR(200) NOT NULL,
    type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    description VARCHAR(1000),
    monthly_rent BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT uq_units_id_organization UNIQUE (id, organization_id),
    CONSTRAINT fk_units_property_tenant FOREIGN KEY (property_id, organization_id)
        REFERENCES properties(id, organization_id),
    CONSTRAINT ck_units_type CHECK (type IN ('APARTMENT', 'ROOM', 'SHOP', 'OFFICE')),
    CONSTRAINT ck_units_status CHECK (status IN ('AVAILABLE', 'OCCUPIED', 'MAINTENANCE')),
    CONSTRAINT ck_units_rent CHECK (monthly_rent >= 0)
);
CREATE UNIQUE INDEX uq_units_property_name_active
    ON units (organization_id, property_id, LOWER(name)) WHERE deleted_at IS NULL;
CREATE INDEX idx_units_property_active ON units (organization_id, property_id) WHERE deleted_at IS NULL;

CREATE TABLE tenants (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    full_name VARCHAR(200) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(320),
    address VARCHAR(500),
    identity_reference VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT uq_tenants_id_organization UNIQUE (id, organization_id)
);
CREATE INDEX idx_tenants_organization_name ON tenants (organization_id, full_name) WHERE deleted_at IS NULL;

CREATE TABLE leases (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    unit_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    monthly_rent BIGINT NOT NULL,
    deposit_amount BIGINT NOT NULL DEFAULT 0,
    due_day SMALLINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_leases_id_organization UNIQUE (id, organization_id),
    CONSTRAINT fk_leases_unit_tenant FOREIGN KEY (unit_id, organization_id) REFERENCES units(id, organization_id),
    CONSTRAINT fk_leases_tenant_tenant FOREIGN KEY (tenant_id, organization_id) REFERENCES tenants(id, organization_id),
    CONSTRAINT ck_leases_dates CHECK (end_date IS NULL OR end_date >= start_date),
    CONSTRAINT ck_leases_amounts CHECK (monthly_rent > 0 AND deposit_amount >= 0),
    CONSTRAINT ck_leases_due_day CHECK (due_day BETWEEN 1 AND 28),
    CONSTRAINT ck_leases_status CHECK (status IN ('DRAFT', 'ACTIVE', 'TERMINATED', 'CANCELLED'))
);
CREATE INDEX idx_leases_unit_status ON leases (organization_id, unit_id, status);

CREATE TABLE rent_charges (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    lease_id UUID NOT NULL,
    period_start DATE NOT NULL,
    due_date DATE NOT NULL,
    amount_due BIGINT NOT NULL,
    amount_paid BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_rent_charges_id_organization UNIQUE (id, organization_id),
    CONSTRAINT uq_rent_charges_period UNIQUE (organization_id, lease_id, period_start),
    CONSTRAINT fk_rent_charges_lease_tenant FOREIGN KEY (lease_id, organization_id) REFERENCES leases(id, organization_id),
    CONSTRAINT ck_rent_charges_amounts CHECK (amount_due > 0 AND amount_paid >= 0 AND amount_paid <= amount_due),
    CONSTRAINT ck_rent_charges_status CHECK (status IN ('UPCOMING', 'DUE', 'PARTIAL', 'PAID', 'OVERDUE', 'CANCELLED'))
);
CREATE INDEX idx_rent_charges_due ON rent_charges (organization_id, status, due_date);

CREATE TABLE payments (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    rent_charge_id UUID NOT NULL,
    amount BIGINT NOT NULL,
    payment_date DATE NOT NULL,
    method VARCHAR(30) NOT NULL,
    reference VARCHAR(120),
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_payments_charge_tenant FOREIGN KEY (rent_charge_id, organization_id)
        REFERENCES rent_charges(id, organization_id),
    CONSTRAINT ck_payments_amount CHECK (amount > 0),
    CONSTRAINT ck_payments_method CHECK (method IN ('CASH', 'BANK_TRANSFER', 'MOBILE_MONEY', 'OTHER')),
    CONSTRAINT ck_payments_status CHECK (status IN ('CONFIRMED', 'CANCELLED'))
);
CREATE INDEX idx_payments_charge ON payments (organization_id, rent_charge_id, payment_date);

CREATE TABLE reminders (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    rent_charge_id UUID NOT NULL,
    phone VARCHAR(20) NOT NULL,
    message VARCHAR(2000) NOT NULL,
    prepared_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_reminders_charge_tenant FOREIGN KEY (rent_charge_id, organization_id)
        REFERENCES rent_charges(id, organization_id)
);

CREATE TABLE receipts (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    rent_charge_id UUID NOT NULL,
    receipt_number VARCHAR(60) NOT NULL,
    issued_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_receipts_number UNIQUE (organization_id, receipt_number),
    CONSTRAINT uq_receipts_charge UNIQUE (organization_id, rent_charge_id),
    CONSTRAINT fk_receipts_charge_tenant FOREIGN KEY (rent_charge_id, organization_id)
        REFERENCES rent_charges(id, organization_id)
);
