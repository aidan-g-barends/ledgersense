CREATE TABLE audit_event (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    -- nullable: a failed login for an unknown email has no org or user
    org_id     UUID         REFERENCES organization (id),
    user_id    UUID         REFERENCES app_user (id),
    -- WHY no CHECK list here: new actions get added often; the Java enum is the guard
    action     VARCHAR(50)  NOT NULL,
    target_id  UUID,
    ip_address VARCHAR(45),           -- 45 = longest possible IPv6 address as text
    detail     VARCHAR(500),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_event_org_created ON audit_event (org_id, created_at DESC);
