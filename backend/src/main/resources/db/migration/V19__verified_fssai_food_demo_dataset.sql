-- Sector-neutral second demo: food business licensing/registration rule.
-- The profile explicitly declares foodBusinessOperator=true; the engine does not infer it.
INSERT INTO regulatory_sources (id, title, url, source_type, verification_status, verified_at, content_hash)
VALUES
('11000000-0000-0000-0000-000000000001',
 'FSSAI Food Business Licensing and Registration',
 'https://fssai.gov.in/business/licensing',
 'OFFICIAL_PORTAL', 'VERIFIED', now(),
 encode(digest('FSSAI official licensing page excerpt captured for prototype verification: every Food Business Operator in India is required to be licensed/registered under the FSS Act and the licensing/registration procedure is governed by the applicable regulations.', 'sha256'),'hex'))
ON CONFLICT (id) DO NOTHING;

INSERT INTO approvals (id, code, name, authority, purpose, source_id, active)
VALUES
('21000000-0000-0000-0000-000000000001', 'FSSAI-LIC-REG', 'Food Business Licence / Registration', 'Food Safety and Standards Authority of India', 'Licensing or registration requirement for a Food Business Operator.', '11000000-0000-0000-0000-000000000001', true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO regulatory_rules (id, code, name, approval_id, source_id, outcome, priority, version_number, active, effective_from)
VALUES
('31000000-0000-0000-0000-000000000001', 'RULE-FSSAI-FBO', 'Food Business Operator licensing or registration requirement', '21000000-0000-0000-0000-000000000001', '11000000-0000-0000-0000-000000000001', 'APPLICABLE', 100, 1, true, '2026-01-01')
ON CONFLICT (id) DO NOTHING;

INSERT INTO rule_conditions (id, rule_id, field, operator, value_type, value, sequence_number)
VALUES
('41000000-0000-0000-0000-000000000001', '31000000-0000-0000-0000-000000000001', 'REGULATORY_ATTRIBUTE', 'EQ', 'STRING', 'foodBusinessOperator=true', 1)
ON CONFLICT (id) DO NOTHING;

INSERT INTO business_profiles (id, business_name, sector, activity, district, midc_unit, investment_inr, pan_number, gstin, employees, power_usage_kw, business_stage, version_number, entity_version, owner_actor, created_at, updated_at, regulatory_attributes)
VALUES
('81000000-0000-0000-0000-000000000001', 'Demo Food Processing Unit', 'Food Processing', 'Packaged food manufacturing', 'Nashik', false, 15000000, NULL, NULL, 35, 120, 'OPERATING', 1, 0, 'applicant', now(), now(), '{"foodBusinessOperator":"true","environmentalConsentRequired":"false","hazardousProcess":"false"}'::jsonb)
ON CONFLICT (id) DO NOTHING;

INSERT INTO business_profile_versions (id, business_profile_id, version_number, snapshot, change_type, captured_by, captured_at)
VALUES
('81000000-0000-0000-0000-000000000011', '81000000-0000-0000-0000-000000000001', 1, '{"businessName":"Demo Food Processing Unit","sector":"Food Processing","activity":"Packaged food manufacturing","district":"Nashik","midcUnit":false,"investmentInr":15000000,"employees":35,"powerUsageKw":120,"businessStage":"OPERATING","regulatoryAttributes":{"foodBusinessOperator":"true","environmentalConsentRequired":"false","hazardousProcess":"false"}}'::jsonb, 'SEEDED_DEMO', 'system', now())
ON CONFLICT (id) DO NOTHING;
