ALTER TABLE business_profiles
    ADD COLUMN IF NOT EXISTS pan_number VARCHAR(10),
    ADD COLUMN IF NOT EXISTS gstin VARCHAR(15);

CREATE TABLE applications (
    id UUID PRIMARY KEY,
    business_profile_id UUID NOT NULL REFERENCES business_profiles(id),
    approval_id UUID NOT NULL REFERENCES approvals(id),
    analysis_run_id UUID NOT NULL REFERENCES analysis_runs(id),
    profile_version BIGINT NOT NULL,
    approval_code_snapshot VARCHAR(80) NOT NULL,
    approval_name_snapshot VARCHAR(250) NOT NULL,
    authority_snapshot VARCHAR(200) NOT NULL,
    matched_rule_code VARCHAR(80),
    source_id UUID REFERENCES regulatory_sources(id),
    status VARCHAR(40) NOT NULL,
    external_reference VARCHAR(100) UNIQUE,
    submitted_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_applications_profile ON applications(business_profile_id, created_at DESC);
CREATE INDEX idx_applications_status ON applications(status, updated_at DESC);

CREATE TABLE application_status_history (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    from_status VARCHAR(40),
    to_status VARCHAR(40) NOT NULL,
    actor VARCHAR(100) NOT NULL,
    reason VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_application_status_history_app ON application_status_history(application_id, created_at ASC);

CREATE TABLE application_queries (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    subject VARCHAR(160) NOT NULL,
    query_text VARCHAR(5000) NOT NULL,
    status VARCHAR(20) NOT NULL,
    raised_by VARCHAR(100) NOT NULL,
    raised_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_application_queries_app ON application_queries(application_id, raised_at ASC);

CREATE TABLE application_query_responses (
    id UUID PRIMARY KEY,
    query_id UUID NOT NULL REFERENCES application_queries(id) ON DELETE CASCADE,
    response_text VARCHAR(5000) NOT NULL,
    responder VARCHAR(100) NOT NULL,
    attachment_document_ids JSONB,
    created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_application_query_responses_query ON application_query_responses(query_id, created_at ASC);

CREATE TABLE inspections (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    scheduled_at TIMESTAMP NOT NULL,
    assigned_officer VARCHAR(100) NOT NULL,
    outcome VARCHAR(30) NOT NULL,
    outcome_notes VARCHAR(5000),
    created_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ
);
CREATE INDEX idx_inspections_app ON inspections(application_id, scheduled_at ASC);

CREATE TABLE application_decisions (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL UNIQUE REFERENCES applications(id) ON DELETE CASCADE,
    outcome VARCHAR(30) NOT NULL,
    decision_notes VARCHAR(5000),
    decided_by VARCHAR(100) NOT NULL,
    cited_rule_code VARCHAR(80),
    cited_source_id UUID,
    decided_at TIMESTAMPTZ NOT NULL
);
