ALTER TABLE verified_evidence
    DROP CONSTRAINT IF EXISTS uq_verified_evidence_profile_field;

ALTER TABLE verified_evidence
    ADD CONSTRAINT uq_verified_evidence_profile_field_version
    UNIQUE (business_profile_id, field_name, profile_version);

CREATE INDEX IF NOT EXISTS idx_verified_evidence_current_field
    ON verified_evidence(business_profile_id, profile_version, field_name, status);
