DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM applications
        WHERE status NOT IN ('REJECTED', 'RENEWAL_DUE')
        GROUP BY business_profile_id, approval_id, analysis_run_id
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Cannot create active application uniqueness index because duplicate active applications exist';
    END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS uq_active_application_per_analysis
    ON applications (business_profile_id, approval_id, analysis_run_id)
    WHERE status NOT IN ('REJECTED', 'RENEWAL_DUE');

DO $$
BEGIN
    IF EXISTS (
        SELECT recipient, type, entity_id
        FROM notifications
        WHERE entity_id IS NOT NULL
        GROUP BY recipient, type, entity_id
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Cannot create notification de-duplication index because duplicate notifications exist';
    END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS uq_notification_entity
    ON notifications (recipient, type, entity_id)
    WHERE entity_id IS NOT NULL;
