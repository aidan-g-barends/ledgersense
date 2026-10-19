CREATE TABLE bank_transaction (
                                  id              UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
                                  org_id          UUID          NOT NULL REFERENCES organization (id),
                                  bank_account_id UUID          NOT NULL REFERENCES bank_account (id),
                                  import_batch_id UUID          NOT NULL REFERENCES import_batch (id),
    -- WHY SET NULL: deleting a category shouldn't delete money records, just uncategorise them
                                  category_id     UUID          REFERENCES category (id) ON DELETE SET NULL,
                                  posted_on       DATE          NOT NULL,
                                  description     VARCHAR(500)  NOT NULL,
                                  amount          NUMERIC(14,2) NOT NULL,   -- exact cents. negative = money out
                                  balance         NUMERIC(14,2),            -- some bank CSVs include it, some don't
                                  fingerprint     CHAR(64)      NOT NULL,
                                  created_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
                                  updated_at      TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- THE duplicate guard: the same row can't land twice in the same account
CREATE UNIQUE INDEX uq_bank_transaction_account_fingerprint
    ON bank_transaction (bank_account_id, fingerprint);

-- For the ledger page: an org's transactions, newest first
CREATE INDEX idx_bank_transaction_org_posted ON bank_transaction (org_id, posted_on DESC);
CREATE INDEX idx_bank_transaction_import_batch ON bank_transaction (import_batch_id);
CREATE INDEX idx_bank_transaction_category ON bank_transaction (category_id);