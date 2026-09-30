ALTER TABLE business_profiles
    ADD COLUMN owner_actor VARCHAR(100);

UPDATE business_profiles SET owner_actor = 'admin' WHERE owner_actor IS NULL;

ALTER TABLE business_profiles
    ALTER COLUMN owner_actor SET NOT NULL;

CREATE TABLE business_documents (
    id UUID PRIMARY KEY,
    business_profile_id UUID NOT NULL REFERENCES business_profiles(id) ON DELETE CASCADE,
    category VARCHAR(80) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(120) NOT NULL,
    declared_size_bytes BIGINT NOT NULL,
    object_key VARCHAR(800) NOT NULL UNIQUE,
    status VARCHAR(30) NOT NULL,
    stored_size_bytes BIGINT,
    e_tag VARCHAR(200),
    uploaded_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_business_documents_profile ON business_documents(business_profile_id, created_at DESC);
