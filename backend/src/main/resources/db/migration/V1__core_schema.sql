CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE business_profiles (
    id UUID PRIMARY KEY,
    business_name VARCHAR(200) NOT NULL,
    sector VARCHAR(100) NOT NULL,
    activity VARCHAR(120) NOT NULL,
    district VARCHAR(100) NOT NULL,
    midc_unit BOOLEAN NOT NULL,
    investment_inr NUMERIC(18,2) NOT NULL,
    employees INTEGER NOT NULL,
    power_usage_kw NUMERIC(12,2) NOT NULL,
    business_stage VARCHAR(30) NOT NULL,
    version_number BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE regulatory_sources (
    id UUID PRIMARY KEY,
    title VARCHAR(300) NOT NULL,
    url VARCHAR(1000) NOT NULL,
    source_type VARCHAR(40) NOT NULL,
    verification_status VARCHAR(20) NOT NULL,
    published_on DATE,
    effective_from DATE,
    expires_on DATE,
    verified_at TIMESTAMPTZ,
    content_hash VARCHAR(128)
);

CREATE TABLE approvals (
    id UUID PRIMARY KEY,
    code VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(250) NOT NULL,
    authority VARCHAR(200) NOT NULL,
    purpose VARCHAR(1000),
    source_id UUID NOT NULL REFERENCES regulatory_sources(id),
    active BOOLEAN NOT NULL
);

CREATE TABLE regulatory_rules (
    id UUID PRIMARY KEY,
    code VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(250) NOT NULL,
    approval_id UUID NOT NULL REFERENCES approvals(id),
    source_id UUID NOT NULL REFERENCES regulatory_sources(id),
    outcome VARCHAR(30) NOT NULL,
    priority INTEGER NOT NULL,
    version_number BIGINT NOT NULL,
    active BOOLEAN NOT NULL,
    effective_from DATE,
    expires_on DATE
);

CREATE TABLE rule_conditions (
    id UUID PRIMARY KEY,
    rule_id UUID NOT NULL REFERENCES regulatory_rules(id) ON DELETE CASCADE,
    field VARCHAR(40) NOT NULL,
    operator VARCHAR(20) NOT NULL,
    value_type VARCHAR(20) NOT NULL,
    value VARCHAR(500) NOT NULL,
    sequence_number INTEGER NOT NULL
);

CREATE INDEX idx_rule_conditions_rule ON rule_conditions(rule_id);
CREATE INDEX idx_rules_active ON regulatory_rules(active);

CREATE TABLE analysis_runs (
    id UUID PRIMARY KEY,
    business_profile_id UUID NOT NULL REFERENCES business_profiles(id),
    profile_version BIGINT NOT NULL,
    rule_set_version VARCHAR(2000) NOT NULL,
    evaluated_at TIMESTAMPTZ NOT NULL,
    result_snapshot JSONB NOT NULL
);

CREATE INDEX idx_analysis_profile ON analysis_runs(business_profile_id, evaluated_at DESC);

CREATE TABLE audit_events (
    id UUID PRIMARY KEY,
    actor VARCHAR(100) NOT NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_audit_entity ON audit_events(entity_type, entity_id, created_at DESC);
