-- Reference operational scenarios for the local judging/demo journey.
-- These records are seeded application states, not synthetic performance claims.

UPDATE business_profiles
SET business_name = 'Reference Manufacturing Unit'
WHERE id = '80000000-0000-0000-0000-000000000001';

UPDATE business_profile_versions
SET snapshot = jsonb_set(snapshot, '{businessName}', '"Reference Manufacturing Unit"'::jsonb)
WHERE id = '80000000-0000-0000-0000-000000000011';

UPDATE business_profiles
SET business_name = 'Reference Food Processing Unit'
WHERE id = '81000000-0000-0000-0000-000000000001';

UPDATE business_profile_versions
SET snapshot = jsonb_set(snapshot, '{businessName}', '"Reference Food Processing Unit"'::jsonb)
WHERE id = '81000000-0000-0000-0000-000000000011';

INSERT INTO business_profiles (
    id, business_name, sector, activity, district, midc_unit, investment_inr,
    pan_number, gstin, employees, power_usage_kw, business_stage, version_number,
    entity_version, owner_actor, created_at, updated_at, regulatory_attributes
)
VALUES
(
    '82000000-0000-0000-0000-000000000001',
    'Reference Expansion Unit',
    'Manufacturing',
    'Industrial process expansion',
    'Pune', false, 45000000, NULL, NULL, 110, 420, 'EXPANSION', 1, 0,
    'applicant', now(), now(),
    '{"environmentalConsentRequired":"true","hazardousProcess":"false","pollutionCategory":"GREEN"}'::jsonb
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO business_profile_versions (
    id, business_profile_id, version_number, snapshot, change_type, captured_by, captured_at
)
VALUES
(
    '82000000-0000-0000-0000-000000000011',
    '82000000-0000-0000-0000-000000000001',
    1,
    '{"businessName":"Reference Expansion Unit","sector":"Manufacturing","activity":"Industrial process expansion","district":"Pune","midcUnit":false,"investmentInr":45000000,"employees":110,"powerUsageKw":420,"businessStage":"EXPANSION","regulatoryAttributes":{"environmentalConsentRequired":"true","hazardousProcess":"false","pollutionCategory":"GREEN"}}'::jsonb,
    'SEEDED_REFERENCE', 'system', now()
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO analysis_runs (id, business_profile_id, profile_version, rule_set_version, evaluated_at, result_snapshot)
VALUES
(
    '90000000-0000-0000-0000-000000000001',
    '80000000-0000-0000-0000-000000000001',
    1, 'verified-catalogue-v20', now() - interval '2 days',
    '{"results":[{"approvalId":"20000000-0000-0000-0000-000000000001","approvalCode":"MPCB-CTE","approvalName":"Consent to Establish","authority":"Maharashtra Pollution Control Board","status":"APPLICABLE","reason":"The reference business declares environmental consent as required and is in SETUP/EXPANSION stage.","ruleCode":"RULE-MPCB-CTE-ENV","sourceId":"10000000-0000-0000-0000-000000000001"},{"approvalId":"20000000-0000-0000-0000-000000000002","approvalCode":"MPCB-CTO","approvalName":"Consent to Operate","authority":"Maharashtra Pollution Control Board","status":"NOT_APPLICABLE","reason":"The current reference business is not yet in OPERATING stage.","ruleCode":"RULE-MPCB-CTO-ENV","sourceId":"10000000-0000-0000-0000-000000000001"}],"scrutinyTier":"STANDARD","scrutinyReasons":[]}'::jsonb
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO analysis_runs (id, business_profile_id, profile_version, rule_set_version, evaluated_at, result_snapshot)
VALUES
(
    '90000000-0000-0000-0000-000000000002',
    '81000000-0000-0000-0000-000000000001',
    1, 'verified-catalogue-v20', now() - interval '2 days',
    '{"results":[{"approvalId":"21000000-0000-0000-0000-000000000001","approvalCode":"FSSAI-LIC-REG","approvalName":"Food Business Licence / Registration","authority":"Food Safety and Standards Authority of India","status":"APPLICABLE","reason":"The reference business explicitly declares foodBusinessOperator=true.","ruleCode":"RULE-FSSAI-FBO","sourceId":"11000000-0000-0000-0000-000000000001"}],"scrutinyTier":"STANDARD","scrutinyReasons":[]}'::jsonb
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO analysis_runs (id, business_profile_id, profile_version, rule_set_version, evaluated_at, result_snapshot)
VALUES
(
    '90000000-0000-0000-0000-000000000003',
    '82000000-0000-0000-0000-000000000001',
    1, 'verified-catalogue-v20', now() - interval '1 day',
    '{"results":[{"approvalId":"20000000-0000-0000-0000-000000000001","approvalCode":"MPCB-CTE","approvalName":"Consent to Establish","authority":"Maharashtra Pollution Control Board","status":"APPLICABLE","reason":"The reference business declares environmental consent as required and is in EXPANSION stage.","ruleCode":"RULE-MPCB-CTE-ENV","sourceId":"10000000-0000-0000-0000-000000000001"},{"approvalId":"20000000-0000-0000-0000-000000000002","approvalCode":"MPCB-CTO","approvalName":"Consent to Operate","authority":"Maharashtra Pollution Control Board","status":"NOT_APPLICABLE","reason":"The reference business is not yet in OPERATING stage.","ruleCode":"RULE-MPCB-CTO-ENV","sourceId":"10000000-0000-0000-0000-000000000001"}],"scrutinyTier":"STANDARD","scrutinyReasons":[]}'::jsonb
)
ON CONFLICT (id) DO NOTHING;

-- A completed approval, useful for the renewal and lifecycle screens.
INSERT INTO applications (
    id, business_profile_id, approval_id, analysis_run_id, profile_version,
    approval_code_snapshot, approval_name_snapshot, authority_snapshot, matched_rule_code,
    source_id, status, external_reference, submitted_at, created_at, updated_at, sla_due_at, entity_version
)
VALUES
(
    '83000000-0000-0000-0000-000000000001',
    '80000000-0000-0000-0000-000000000001',
    '20000000-0000-0000-0000-000000000001',
    '90000000-0000-0000-0000-000000000001', 1,
    'MPCB-CTE', 'Consent to Establish', 'Maharashtra Pollution Control Board', 'RULE-MPCB-CTE-ENV',
    '10000000-0000-0000-0000-000000000001', 'APPROVED', 'ANU-REF-CTE-001',
    now() - interval '50 days', now() - interval '50 days', now() - interval '5 days', now() - interval '20 days', 0
)
ON CONFLICT (id) DO NOTHING;

-- Inspection scheduled: a live workflow state for the officer-side recording.
INSERT INTO applications (
    id, business_profile_id, approval_id, analysis_run_id, profile_version,
    approval_code_snapshot, approval_name_snapshot, authority_snapshot, matched_rule_code,
    source_id, status, external_reference, submitted_at, created_at, updated_at, entity_version
)
VALUES
(
    '83000000-0000-0000-0000-000000000002',
    '80000000-0000-0000-0000-000000000001',
    '20000000-0000-0000-0000-000000000002',
    '90000000-0000-0000-0000-000000000001', 1,
    'MPCB-CTO', 'Consent to Operate', 'Maharashtra Pollution Control Board', 'RULE-MPCB-CTO-ENV',
    '10000000-0000-0000-0000-000000000001', 'INSPECTION_SCHEDULED', 'ANU-REF-CTO-002',
    now() - interval '12 days', now() - interval '12 days', now() - interval '1 day', 0
)
ON CONFLICT (id) DO NOTHING;

-- Query waiting for applicant response: useful for the applicant/officer hand-off.
INSERT INTO applications (
    id, business_profile_id, approval_id, analysis_run_id, profile_version,
    approval_code_snapshot, approval_name_snapshot, authority_snapshot, matched_rule_code,
    source_id, status, external_reference, submitted_at, created_at, updated_at, sla_due_at, entity_version
)
VALUES
(
    '83000000-0000-0000-0000-000000000003',
    '81000000-0000-0000-0000-000000000001',
    '21000000-0000-0000-0000-000000000001',
    '90000000-0000-0000-0000-000000000002', 1,
    'FSSAI-LIC-REG', 'Food Business Licence / Registration', 'Food Safety and Standards Authority of India', 'RULE-FSSAI-FBO',
    '11000000-0000-0000-0000-000000000001', 'QUERY_RAISED', 'ANU-REF-FSSAI-003',
    now() - interval '7 days', now() - interval '7 days', now() - interval '3 hours', NULL, 0
)
ON CONFLICT (id) DO NOTHING;

-- Scrutiny lane with an SLA due inside the warning window.
INSERT INTO applications (
    id, business_profile_id, approval_id, analysis_run_id, profile_version,
    approval_code_snapshot, approval_name_snapshot, authority_snapshot, matched_rule_code,
    source_id, status, external_reference, submitted_at, created_at, updated_at, sla_due_at, entity_version
)
VALUES
(
    '83000000-0000-0000-0000-000000000004',
    '82000000-0000-0000-0000-000000000001',
    '20000000-0000-0000-0000-000000000001',
    '90000000-0000-0000-0000-000000000003', 1,
    'MPCB-CTE', 'Consent to Establish', 'Maharashtra Pollution Control Board', 'RULE-MPCB-CTE-ENV',
    '10000000-0000-0000-0000-000000000001', 'UNDER_SCRUTINY', 'ANU-REF-CTE-004',
    now() - interval '25 days', now() - interval '25 days', now() - interval '1 hour', now() + interval '5 days', 0
)
ON CONFLICT (id) DO NOTHING;

-- Dependency lane: CTO is submitted before the prerequisite CTE is approved.
INSERT INTO applications (
    id, business_profile_id, approval_id, analysis_run_id, profile_version,
    approval_code_snapshot, approval_name_snapshot, authority_snapshot, matched_rule_code,
    source_id, status, external_reference, submitted_at, created_at, updated_at, sla_due_at, entity_version
)
VALUES
(
    '83000000-0000-0000-0000-000000000005',
    '82000000-0000-0000-0000-000000000001',
    '20000000-0000-0000-0000-000000000002',
    '90000000-0000-0000-0000-000000000003', 1,
    'MPCB-CTO', 'Consent to Operate', 'Maharashtra Pollution Control Board', 'RULE-MPCB-CTO-ENV',
    '10000000-0000-0000-0000-000000000001', 'SUBMITTED', 'ANU-REF-CTO-005',
    now() - interval '2 days', now() - interval '2 days', now() - interval '2 days', NULL, 0
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO application_status_history (id, application_id, from_status, to_status, actor, reason, created_at)
VALUES
('84000000-0000-0000-0000-000000000001','83000000-0000-0000-0000-000000000001',NULL,'SUBMITTED','applicant','Reference application submitted.',now() - interval '50 days'),
('84000000-0000-0000-0000-000000000002','83000000-0000-0000-0000-000000000001','SUBMITTED','UNDER_SCRUTINY','officer','Initial scrutiny started.',now() - interval '48 days'),
('84000000-0000-0000-0000-000000000003','83000000-0000-0000-0000-000000000001','UNDER_SCRUTINY','DECISION','officer','Evidence review completed.',now() - interval '23 days'),
('84000000-0000-0000-0000-000000000004','83000000-0000-0000-0000-000000000001','DECISION','APPROVED','officer','Approval decision recorded.',now() - interval '20 days'),
('84000000-0000-0000-0000-000000000005','83000000-0000-0000-0000-000000000002',NULL,'SUBMITTED','applicant','Reference application submitted.',now() - interval '12 days'),
('84000000-0000-0000-0000-000000000006','83000000-0000-0000-0000-000000000002','SUBMITTED','UNDER_SCRUTINY','officer','Initial scrutiny started.',now() - interval '11 days'),
('84000000-0000-0000-0000-000000000007','83000000-0000-0000-0000-000000000002','UNDER_SCRUTINY','INSPECTION_SCHEDULED','officer','Inspection scheduled after scrutiny.',now() - interval '1 day'),
('84000000-0000-0000-0000-000000000008','83000000-0000-0000-0000-000000000003',NULL,'SUBMITTED','applicant','Reference application submitted.',now() - interval '7 days'),
('84000000-0000-0000-0000-000000000009','83000000-0000-0000-0000-000000000003','SUBMITTED','UNDER_SCRUTINY','officer','Initial scrutiny started.',now() - interval '6 days'),
('84000000-0000-0000-0000-000000000010','83000000-0000-0000-0000-000000000003','UNDER_SCRUTINY','QUERY_RAISED','officer','Additional operating evidence requested.',now() - interval '3 hours'),
('84000000-0000-0000-0000-000000000011','83000000-0000-0000-0000-000000000004',NULL,'SUBMITTED','applicant','Reference application submitted.',now() - interval '25 days'),
('84000000-0000-0000-0000-000000000012','83000000-0000-0000-0000-000000000004','SUBMITTED','UNDER_SCRUTINY','officer','Application is in departmental scrutiny.',now() - interval '24 days'),
('84000000-0000-0000-0000-000000000013','83000000-0000-0000-0000-000000000005',NULL,'SUBMITTED','applicant','Reference application submitted.',now() - interval '2 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO application_decisions (id, application_id, outcome, decision_notes, decided_by, cited_rule_code, cited_source_id, decided_at, valid_from, valid_until)
VALUES
(
    '85000000-0000-0000-0000-000000000001', '83000000-0000-0000-0000-000000000001',
    'APPROVED', 'Reference approval completed after evidence review.', 'officer',
    'RULE-MPCB-CTE-ENV', '10000000-0000-0000-0000-000000000001', now() - interval '20 days', current_date - 20, current_date + 10
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO inspections (id, application_id, scheduled_at, assigned_officer, outcome, outcome_notes, created_at, completed_at)
VALUES
(
    '86000000-0000-0000-0000-000000000001', '83000000-0000-0000-0000-000000000002',
    localtimestamp + interval '1 day', 'Officer Mehta', 'PENDING', NULL, now() - interval '1 day', NULL
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO application_queries (id, application_id, subject, query_text, status, raised_by, raised_at, updated_at)
VALUES
(
    '87000000-0000-0000-0000-000000000001', '83000000-0000-0000-0000-000000000003',
    'Clarify operating evidence', 'Please provide the latest operating-premises evidence for review.',
    'OPEN', 'officer', now() - interval '3 hours', now() - interval '3 hours'
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO renewals (
    id, application_id, business_profile_id, approval_id, valid_from, valid_until,
    reminder_days, status, source_id, created_at, updated_at
)
VALUES
(
    '88000000-0000-0000-0000-000000000001', '83000000-0000-0000-0000-000000000001',
    '80000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001',
    current_date - 20, current_date + 10, 30, 'DUE',
    '10000000-0000-0000-0000-000000000003', now() - interval '20 days', now()
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO grievances (
    id, business_profile_id, application_id, subject, description, department,
    priority, status, assigned_to, created_at, updated_at
)
VALUES
(
    '89000000-0000-0000-0000-000000000001', '81000000-0000-0000-0000-000000000001',
    '83000000-0000-0000-0000-000000000003',
    'Application query follow-up',
    'Applicant requested clarification on the query response timeline.',
    'Food Safety and Standards Authority of India', 'MEDIUM', 'OPEN', 'officer', now() - interval '1 day', now() - interval '1 hour'
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO compliance_obligations (
    id, business_profile_id, application_id, approval_id, name, authority,
    description, due_date, reminder_days, frequency, status, source_id, created_at, updated_at
)
VALUES
(
    '8a000000-0000-0000-0000-000000000001', '80000000-0000-0000-0000-000000000001',
    '83000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001',
    'Maintain consent-linked operating records', 'Maharashtra Pollution Control Board',
    'Reference compliance item for the operating lifecycle.', current_date + 4, 7, 'ANNUAL', 'DUE',
    '10000000-0000-0000-0000-000000000003', now() - interval '10 days', now()
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO notifications (id, recipient, type, title, body, entity_type, entity_id, created_at)
VALUES
('8b000000-0000-0000-0000-000000000001','applicant','APPLICATION_QUERY','Application query requires attention','A department query is open for Reference Food Processing Unit.','Application','83000000-0000-0000-0000-000000000003',now() - interval '3 hours'),
('8b000000-0000-0000-0000-000000000002','officer','SLA_AT_RISK','Consent application approaching service warning window','Reference Expansion Unit is approaching the configured service warning window.','Application','83000000-0000-0000-0000-000000000004',now() - interval '2 hours')
ON CONFLICT (id) DO NOTHING;
