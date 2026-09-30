CREATE TABLE approval_dependencies (
    id UUID PRIMARY KEY,
    approval_id UUID NOT NULL REFERENCES approvals(id) ON DELETE CASCADE,
    depends_on_approval_id UUID NOT NULL REFERENCES approvals(id) ON DELETE CASCADE,
    dependency_type VARCHAR(30) NOT NULL,
    reason VARCHAR(500),
    active BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_approval_dependency UNIQUE (approval_id, depends_on_approval_id),
    CONSTRAINT chk_no_self_dependency CHECK (approval_id <> depends_on_approval_id)
);

CREATE INDEX idx_approval_dependencies_approval ON approval_dependencies(approval_id, active);
CREATE INDEX idx_approval_dependencies_depends_on ON approval_dependencies(depends_on_approval_id, active);

CREATE TABLE approval_document_requirements (
    id UUID PRIMARY KEY,
    approval_id UUID NOT NULL REFERENCES approvals(id) ON DELETE CASCADE,
    category VARCHAR(80) NOT NULL,
    document_name VARCHAR(250) NOT NULL,
    description VARCHAR(1000),
    mandatory BOOLEAN NOT NULL,
    active BOOLEAN NOT NULL,
    source_id UUID REFERENCES regulatory_sources(id),
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_approval_document_requirement UNIQUE (approval_id, category)
);

CREATE INDEX idx_approval_document_requirements_approval ON approval_document_requirements(approval_id, active);

ALTER TABLE business_documents
    ADD COLUMN normalized_category VARCHAR(80);

UPDATE business_documents SET normalized_category = UPPER(TRIM(category)) WHERE normalized_category IS NULL;
ALTER TABLE business_documents ALTER COLUMN normalized_category SET NOT NULL;
CREATE INDEX idx_business_documents_profile_category ON business_documents(business_profile_id, normalized_category, status);
