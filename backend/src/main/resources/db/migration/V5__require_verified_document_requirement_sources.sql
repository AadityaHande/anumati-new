UPDATE approval_document_requirements r
SET source_id = a.source_id
FROM approvals a
WHERE r.approval_id = a.id
  AND r.source_id IS NULL;

ALTER TABLE approval_document_requirements
    ALTER COLUMN source_id SET NOT NULL;
