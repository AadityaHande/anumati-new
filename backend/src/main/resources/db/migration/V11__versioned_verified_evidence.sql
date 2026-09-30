ALTER TABLE verified_evidence ADD COLUMN IF NOT EXISTS profile_version BIGINT NOT NULL DEFAULT 1;
CREATE INDEX IF NOT EXISTS idx_verified_evidence_profile_version ON verified_evidence(business_profile_id, profile_version, status);
