CREATE TABLE IF NOT EXISTS business_profile_versions (
    id UUID PRIMARY KEY,
    business_profile_id UUID NOT NULL REFERENCES business_profiles(id),
    version_number BIGINT NOT NULL,
    snapshot JSONB NOT NULL,
    change_type VARCHAR(40) NOT NULL,
    captured_by VARCHAR(200) NOT NULL,
    captured_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_business_profile_version UNIQUE (business_profile_id, version_number)
);

INSERT INTO business_profile_versions (
    id,
    business_profile_id,
    version_number,
    snapshot,
    change_type,
    captured_by,
    captured_at
)
SELECT
    gen_random_uuid(),
    id,
    version_number,
    jsonb_build_object(
        'businessName', business_name,
        'sector', sector,
        'activity', activity,
        'district', district,
        'midcUnit', midc_unit,
        'investmentInr', investment_inr,
        'panNumber', pan_number,
        'gstin', gstin,
        'employees', employees,
        'powerUsageKw', power_usage_kw,
        'businessStage', business_stage
    ),
    'BACKFILLED',
    owner_actor,
    COALESCE(updated_at, created_at)
FROM business_profiles bp
WHERE NOT EXISTS (
    SELECT 1
    FROM business_profile_versions v
    WHERE v.business_profile_id = bp.id
      AND v.version_number = bp.version_number
);

CREATE INDEX IF NOT EXISTS idx_business_profile_versions_profile
    ON business_profile_versions (business_profile_id, version_number DESC);

ALTER TABLE business_profiles
    ADD COLUMN IF NOT EXISTS entity_version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE applications
    ADD COLUMN IF NOT EXISTS entity_version BIGINT NOT NULL DEFAULT 0;
