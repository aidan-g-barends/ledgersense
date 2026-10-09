-- "user" is a reserved word in Postgres, so the table is app_user
CREATE TABLE app_user (
    id             UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    -- every user belongs to one organization (foreign key)
    org_id         UUID          NOT NULL REFERENCES organization (id),
    email          VARCHAR(254)  NOT NULL,
    password_hash  VARCHAR(255)  NOT NULL,
    role           VARCHAR(20)   NOT NULL,
    last_login_at  TIMESTAMPTZ,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT chk_app_user_role CHECK (role IN ('OWNER')),
    -- emails are always stored lowercase, so Aidan@x.com and aidan@x.com can't both exist
    CONSTRAINT chk_app_user_email_lowercase CHECK (email = lower(email))
);

-- one account per email
CREATE UNIQUE INDEX uq_app_user_email ON app_user (email);
-- Postgres doesn't auto-index foreign keys; this makes "users in org X" fast
CREATE INDEX idx_app_user_org_id ON app_user (org_id);
