CREATE TABLE organization (
    id             UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    name           VARCHAR(150)  NOT NULL,
    base_currency  CHAR(3)       NOT NULL DEFAULT 'ZAR',
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT chk_organization_base_currency
        CHECK (base_currency ~ '^[A-Z]{3}$')
);
