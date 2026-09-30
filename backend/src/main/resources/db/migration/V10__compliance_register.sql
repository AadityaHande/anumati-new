CREATE TABLE compliance_obligations (
 id UUID PRIMARY KEY,
 business_profile_id UUID NOT NULL REFERENCES business_profiles(id) ON DELETE CASCADE,
 application_id UUID REFERENCES applications(id) ON DELETE SET NULL,
 approval_id UUID REFERENCES approvals(id),
 name VARCHAR(250) NOT NULL,
 authority VARCHAR(200) NOT NULL,
 description VARCHAR(1500),
 due_date DATE NOT NULL,
 reminder_days INTEGER NOT NULL CHECK (reminder_days >= 0),
 frequency VARCHAR(30) NOT NULL,
 status VARCHAR(30) NOT NULL,
 source_id UUID REFERENCES regulatory_sources(id),
 completed_at TIMESTAMPTZ,
 created_at TIMESTAMPTZ NOT NULL,
 updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_compliance_profile_due ON compliance_obligations(business_profile_id,due_date);
CREATE INDEX idx_compliance_status_due ON compliance_obligations(status,due_date);
