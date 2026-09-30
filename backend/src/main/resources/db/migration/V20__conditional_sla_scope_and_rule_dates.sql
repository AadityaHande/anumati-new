ALTER TABLE approval_sla_configs
    ADD COLUMN IF NOT EXISTS max_investment_inr NUMERIC(18,2);

ALTER TABLE approval_sla_configs
    DROP CONSTRAINT IF EXISTS ck_approval_sla_max_investment;

ALTER TABLE approval_sla_configs
    ADD CONSTRAINT ck_approval_sla_max_investment CHECK (max_investment_inr IS NULL OR max_investment_inr >= 0);

UPDATE approval_sla_configs
SET max_investment_inr = 1000000000
WHERE id = '70000000-0000-0000-0000-000000000001';

-- The prototype source pages do not publish an effective date for these captured facts.
-- Do not manufacture one; source verification controls whether the rule is active.
UPDATE regulatory_rules
SET effective_from = NULL
WHERE id IN (
    '30000000-0000-0000-0000-000000000001',
    '30000000-0000-0000-0000-000000000002',
    '31000000-0000-0000-0000-000000000001'
);
