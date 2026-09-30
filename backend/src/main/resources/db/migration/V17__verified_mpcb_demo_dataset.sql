-- Source-backed prototype data for Maharashtra. The dataset is intentionally small.
INSERT INTO regulatory_sources (id, title, url, source_type, verification_status, verified_at, content_hash)
VALUES
('10000000-0000-0000-0000-000000000001',
 'MPCB Consent Under Water & Air Act',
 'https://www.mpcb.gov.in/en/consentmgt/water-and-air-act',
 'OFFICIAL_PORTAL', 'VERIFIED', now(), encode(digest('MPCB consent management excerpt captured for prototype verification: Consent to Establish is obtained prior to establishing an industry or process; Consent to Operate is obtained once the industry or process plant is established with required pollution control systems.', 'sha256'),'hex'))
ON CONFLICT (id) DO NOTHING;

INSERT INTO regulatory_sources (id, title, url, source_type, verification_status, verified_at, content_hash)
VALUES
('10000000-0000-0000-0000-000000000002',
 'MPCB Information to be submitted with the Application',
 'https://mpcb.gov.in/en/node/4201',
 'OFFICIAL_PORTAL', 'VERIFIED', now(), encode(digest('MPCB application information excerpt captured for prototype verification: Consent to establish documents include CA Certificate, Balance Sheet, Capital Investment, Manufacturing Process, Industry Registration, Land Ownership Certificate and detailed pollution control system proposal; Consent to Operate/Renewal also lists Previous Consent Copy.', 'sha256'),'hex'))
ON CONFLICT (id) DO NOTHING;

INSERT INTO regulatory_sources (id, title, url, source_type, verification_status, verified_at, content_hash)
VALUES
('10000000-0000-0000-0000-000000000003',
 'MPCB Notified Public Service Timelines',
 'https://www.mpcb.gov.in/en/node/6866',
 'OFFICIAL_PORTAL', 'VERIFIED', now(), encode(digest('MPCB notified services excerpt captured for prototype verification: category and capital-investment based time limits are published for Consent to Establish/Operate/Renewal; the Green category Consent to Establish row includes 30 days for capital investment up to Rs. 100 crore.', 'sha256'),'hex'))
ON CONFLICT (id) DO NOTHING;

INSERT INTO approvals (id, code, name, authority, purpose, source_id, active)
VALUES
('20000000-0000-0000-0000-000000000001', 'MPCB-CTE', 'Consent to Establish', 'Maharashtra Pollution Control Board', 'Consent before establishing an industry or process when environmental consent is required.', '10000000-0000-0000-0000-000000000001', true),
('20000000-0000-0000-0000-000000000002', 'MPCB-CTO', 'Consent to Operate', 'Maharashtra Pollution Control Board', 'Consent before commencing production or operation after establishment when environmental consent is required.', '10000000-0000-0000-0000-000000000001', true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO regulatory_rules (id, code, name, approval_id, source_id, outcome, priority, version_number, active, effective_from)
VALUES
('30000000-0000-0000-0000-000000000001', 'RULE-MPCB-CTE-ENV', 'Environmental consent required before establishment', '20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'APPLICABLE', 100, 1, true, '2026-01-01'),
('30000000-0000-0000-0000-000000000002', 'RULE-MPCB-CTO-ENV', 'Environmental consent required before operation', '20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001', 'APPLICABLE', 100, 1, true, '2026-01-01')
ON CONFLICT (id) DO NOTHING;

INSERT INTO rule_conditions (id, rule_id, field, operator, value_type, value, sequence_number)
VALUES
('40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 'REGULATORY_ATTRIBUTE', 'EQ', 'STRING', 'environmentalConsentRequired=true', 1),
('40000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001', 'BUSINESS_STAGE', 'IN', 'STRING', 'SETUP,EXPANSION', 2),
('40000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000002', 'REGULATORY_ATTRIBUTE', 'EQ', 'STRING', 'environmentalConsentRequired=true', 1),
('40000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000002', 'BUSINESS_STAGE', 'EQ', 'STRING', 'OPERATING', 2)
ON CONFLICT (id) DO NOTHING;

INSERT INTO approval_document_requirements (id, approval_id, category, document_name, description, mandatory, active, source_id, created_at)
VALUES
('50000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 'CA_CERTIFICATE', 'CA Certificate / Balance Sheet', 'Capital investment evidence for consent application.', true, true, '10000000-0000-0000-0000-000000000002', now()),
('50000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000001', 'MANUFACTURING_PROCESS', 'Manufacturing Process', 'Manufacturing process description submitted to MPCB.', true, true, '10000000-0000-0000-0000-000000000002', now()),
('50000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000001', 'INDUSTRY_REGISTRATION', 'Industry Registration', 'Industry registration evidence.', true, true, '10000000-0000-0000-0000-000000000002', now()),
('50000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000001', 'LAND_OWNERSHIP', 'Land Ownership Certificate', 'Evidence of land or premises ownership or lawful possession.', true, true, '10000000-0000-0000-0000-000000000002', now()),
('50000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000001', 'POLLUTION_CONTROL_PROPOSAL', 'Pollution Control System Proposal', 'Detailed proposal of the pollution control system.', true, true, '10000000-0000-0000-0000-000000000002', now()),
('50000000-0000-0000-0000-000000000006', '20000000-0000-0000-0000-000000000002', 'CA_CERTIFICATE', 'CA Certificate / Balance Sheet', 'Capital investment evidence for consent application.', true, true, '10000000-0000-0000-0000-000000000002', now()),
('50000000-0000-0000-0000-000000000007', '20000000-0000-0000-0000-000000000002', 'MANUFACTURING_PROCESS', 'Manufacturing Process', 'Manufacturing process description submitted to MPCB.', true, true, '10000000-0000-0000-0000-000000000002', now()),
('50000000-0000-0000-0000-000000000008', '20000000-0000-0000-0000-000000000002', 'INDUSTRY_REGISTRATION', 'Industry Registration', 'Industry registration evidence.', true, true, '10000000-0000-0000-0000-000000000002', now()),
('50000000-0000-0000-0000-000000000009', '20000000-0000-0000-0000-000000000002', 'LAND_OWNERSHIP', 'Land Ownership Certificate', 'Evidence of land or premises ownership or lawful possession.', true, true, '10000000-0000-0000-0000-000000000002', now()),
('50000000-0000-0000-0000-000000000010', '20000000-0000-0000-0000-000000000002', 'POLLUTION_CONTROL_PROPOSAL', 'Pollution Control System Proposal', 'Detailed proposal of the pollution control system.', true, true, '10000000-0000-0000-0000-000000000002', now()),
('50000000-0000-0000-0000-000000000011', '20000000-0000-0000-0000-000000000002', 'PREVIOUS_CONSENT', 'Previous Consent Copy', 'Previous consent copy for Consent to Operate or Renewal where applicable.', false, true, '10000000-0000-0000-0000-000000000002', now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO approval_dependencies (id, approval_id, depends_on_approval_id, dependency_type, reason, active, created_at)
VALUES
('60000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000001', 'BLOCKING', 'Consent to Establish precedes Consent to Operate where both are required.', true, now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO approval_sla_configs (id, approval_id, target_hours, warning_hours, source_id, active, updated_at)
VALUES
('70000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 720, 168, '10000000-0000-0000-0000-000000000003', true, now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO business_profiles (id, business_name, sector, activity, district, midc_unit, investment_inr, pan_number, gstin, employees, power_usage_kw, business_stage, version_number, entity_version, owner_actor, created_at, updated_at, regulatory_attributes)
VALUES
('80000000-0000-0000-0000-000000000001', 'Demo Process Industries', 'Manufacturing', 'Industrial process operations', 'Pune', false, 20000000, NULL, NULL, 50, 250, 'SETUP', 1, 0, 'applicant', now(), now(), '{"environmentalConsentRequired":"true","hazardousProcess":"false","pollutionCategory":"GREEN"}'::jsonb)
ON CONFLICT (id) DO NOTHING;

INSERT INTO business_profile_versions (id, business_profile_id, version_number, snapshot, change_type, captured_by, captured_at)
VALUES
('80000000-0000-0000-0000-000000000011', '80000000-0000-0000-0000-000000000001', 1, '{"businessName":"Demo Process Industries","sector":"Manufacturing","activity":"Industrial process operations","district":"Pune","midcUnit":false,"investmentInr":20000000,"employees":50,"powerUsageKw":250,"businessStage":"SETUP","regulatoryAttributes":{"environmentalConsentRequired":"true","hazardousProcess":"false","pollutionCategory":"GREEN"}}'::jsonb, 'SEEDED_DEMO', 'system', now())
ON CONFLICT (id) DO NOTHING;
