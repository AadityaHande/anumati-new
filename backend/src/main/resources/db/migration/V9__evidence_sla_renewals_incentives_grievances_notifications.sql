ALTER TABLE applications
    ADD COLUMN IF NOT EXISTS sla_due_at TIMESTAMPTZ;
CREATE INDEX IF NOT EXISTS idx_applications_sla_due_at ON applications(sla_due_at);

ALTER TABLE application_decisions
    ADD COLUMN IF NOT EXISTS valid_from DATE,
    ADD COLUMN IF NOT EXISTS valid_until DATE;

CREATE TABLE verified_evidence (
    id UUID PRIMARY KEY,
    business_profile_id UUID NOT NULL REFERENCES business_profiles(id) ON DELETE CASCADE,
    field_name VARCHAR(80) NOT NULL,
    field_value VARCHAR(500) NOT NULL,
    source_document_id UUID NOT NULL REFERENCES business_documents(id),
    verified_by VARCHAR(100) NOT NULL,
    verified_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT uq_verified_evidence_profile_field UNIQUE (business_profile_id, field_name)
);
CREATE INDEX idx_verified_evidence_profile ON verified_evidence(business_profile_id, status);

CREATE TABLE approval_sla_configs (
    id UUID PRIMARY KEY,
    approval_id UUID NOT NULL UNIQUE REFERENCES approvals(id) ON DELETE CASCADE,
    target_hours INTEGER NOT NULL CHECK (target_hours > 0),
    warning_hours INTEGER NOT NULL CHECK (warning_hours >= 0 AND warning_hours < target_hours),
    source_id UUID NOT NULL REFERENCES regulatory_sources(id),
    active BOOLEAN NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE renewals (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL UNIQUE REFERENCES applications(id) ON DELETE CASCADE,
    business_profile_id UUID NOT NULL REFERENCES business_profiles(id) ON DELETE CASCADE,
    approval_id UUID NOT NULL REFERENCES approvals(id),
    valid_from DATE,
    valid_until DATE NOT NULL,
    reminder_days INTEGER NOT NULL CHECK (reminder_days >= 0),
    status VARCHAR(30) NOT NULL,
    source_id UUID REFERENCES regulatory_sources(id),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_renewals_profile_due ON renewals(business_profile_id, valid_until);

CREATE TABLE incentive_schemes (
    id UUID PRIMARY KEY,
    code VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(250) NOT NULL,
    authority VARCHAR(200) NOT NULL,
    benefit_summary VARCHAR(1500) NOT NULL,
    application_url VARCHAR(1000),
    source_id UUID NOT NULL REFERENCES regulatory_sources(id),
    active BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE incentive_conditions (
    id UUID PRIMARY KEY,
    scheme_id UUID NOT NULL REFERENCES incentive_schemes(id) ON DELETE CASCADE,
    field VARCHAR(40) NOT NULL,
    operator VARCHAR(20) NOT NULL,
    value_type VARCHAR(20) NOT NULL,
    value VARCHAR(500) NOT NULL,
    sequence_number INTEGER NOT NULL
);
CREATE INDEX idx_incentive_conditions_scheme ON incentive_conditions(scheme_id, sequence_number);

CREATE TABLE grievances (
    id UUID PRIMARY KEY,
    business_profile_id UUID NOT NULL REFERENCES business_profiles(id) ON DELETE CASCADE,
    application_id UUID REFERENCES applications(id) ON DELETE SET NULL,
    subject VARCHAR(200) NOT NULL,
    description VARCHAR(5000) NOT NULL,
    department VARCHAR(200),
    priority VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL,
    assigned_to VARCHAR(100),
    resolution VARCHAR(5000),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    closed_at TIMESTAMPTZ
);
CREATE INDEX idx_grievances_profile ON grievances(business_profile_id, created_at DESC);
CREATE INDEX idx_grievances_status ON grievances(status, updated_at DESC);

CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    recipient VARCHAR(120) NOT NULL,
    type VARCHAR(40) NOT NULL,
    title VARCHAR(200) NOT NULL,
    body VARCHAR(2000) NOT NULL,
    entity_type VARCHAR(80),
    entity_id UUID,
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_notifications_recipient ON notifications(recipient, created_at DESC);
