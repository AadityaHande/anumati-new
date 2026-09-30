ALTER TABLE approval_sla_configs
    ADD COLUMN IF NOT EXISTS scope_attributes JSONB NOT NULL DEFAULT '{}'::jsonb;

CREATE INDEX IF NOT EXISTS idx_approval_sla_scope_attributes
    ON approval_sla_configs USING GIN(scope_attributes);

UPDATE regulatory_sources
SET url = 'https://www.mpcb.gov.in/en/node/6866'
WHERE id = '10000000-0000-0000-0000-000000000003';

UPDATE approval_sla_configs
SET scope_attributes = '{"pollutionCategory":"GREEN"}'::jsonb
WHERE id = '70000000-0000-0000-0000-000000000001';
