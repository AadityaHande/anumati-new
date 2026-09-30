ALTER TABLE business_documents
    ADD COLUMN IF NOT EXISTS validated_profile_version BIGINT;

CREATE INDEX IF NOT EXISTS idx_business_documents_validated_profile
    ON business_documents (business_profile_id, validated_profile_version, normalized_category, status);

ALTER TABLE compliance_obligations
    ALTER COLUMN source_id SET NOT NULL;
