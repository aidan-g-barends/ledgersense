CREATE TABLE category (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id     UUID        NOT NULL REFERENCES organization (id),
    name       VARCHAR(50) NOT NULL,
    type       VARCHAR(10) NOT NULL CHECK (type IN ('EXPENSE', 'INCOME')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- WHY lower(name): an index on an expression. "Fuel" and "FUEL" both become "fuel",
-- so the database itself blocks case-only duplicates.
CREATE UNIQUE INDEX uq_category_org_name ON category (org_id, lower(name));
