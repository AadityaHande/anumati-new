ALTER TABLE business_profiles
    ADD COLUMN IF NOT EXISTS regulatory_attributes JSONB NOT NULL DEFAULT '{}'::jsonb;

CREATE INDEX IF NOT EXISTS idx_business_profiles_regulatory_attributes
    ON business_profiles USING GIN (regulatory_attributes);
