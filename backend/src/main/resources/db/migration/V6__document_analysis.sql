CREATE TABLE document_extractions (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL UNIQUE REFERENCES business_documents(id) ON DELETE CASCADE,
    engine VARCHAR(40) NOT NULL,
    status VARCHAR(30) NOT NULL,
    extracted_text TEXT,
    extracted_fields_json JSONB NOT NULL,
    analyzed_at TIMESTAMPTZ NOT NULL,
    error_message VARCHAR(1000)
);

CREATE INDEX idx_document_extractions_status ON document_extractions(status);

CREATE TABLE document_consistency_checks (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL REFERENCES business_documents(id) ON DELETE CASCADE,
    field_name VARCHAR(80) NOT NULL,
    profile_value VARCHAR(500),
    document_value VARCHAR(500),
    status VARCHAR(30) NOT NULL,
    reason VARCHAR(500),
    checked_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_document_consistency_field UNIQUE (document_id, field_name)
);

CREATE INDEX idx_document_consistency_document ON document_consistency_checks(document_id, status);
