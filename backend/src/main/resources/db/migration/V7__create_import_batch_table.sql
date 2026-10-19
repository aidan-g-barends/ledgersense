CREATE TABLE import_batch (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id          UUID         NOT NULL REFERENCES organization (id),
    bank_account_id UUID         NOT NULL REFERENCES bank_account (id),
    file_name       VARCHAR(255) NOT NULL,
    status          VARCHAR(20)  NOT NULL CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED')),
    rows_total      INT          NOT NULL DEFAULT 0 CHECK (rows_total >= 0),
    rows_imported   INT          NOT NULL DEFAULT 0 CHECK (rows_imported >= 0),
    rows_skipped    INT          NOT NULL DEFAULT 0 CHECK (rows_skipped >= 0),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    completed_at    TIMESTAMPTZ
);

-- For the "import history" page: newest first, per org
CREATE INDEX idx_import_batch_org_created ON import_batch (org_id, created_at DESC);
CREATE INDEX idx_import_batch_bank_account ON import_batch (bank_account_id);
