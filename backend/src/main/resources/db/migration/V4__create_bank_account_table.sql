CREATE TABLE bank_account (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id        UUID         NOT NULL REFERENCES organization (id),
    name          VARCHAR(100) NOT NULL,
    bank          VARCHAR(30)  NOT NULL
                  CHECK (bank IN ('FNB', 'ABSA', 'STANDARD_BANK', 'NEDBANK', 'CAPITEC', 'OTHER')),
    account_last4 CHAR(4)      NOT NULL CHECK (account_last4 ~ '^[0-9]{4}$'),
    currency      CHAR(3)      NOT NULL DEFAULT 'ZAR' CHECK (currency ~ '^[A-Z]{3}$'),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Also serves as the org_id index, because org_id is the first column.
CREATE UNIQUE INDEX uq_bank_account_org_bank_last4
    ON bank_account (org_id, bank, account_last4);
