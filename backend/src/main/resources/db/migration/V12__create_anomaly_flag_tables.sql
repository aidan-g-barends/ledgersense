CREATE TABLE anomaly_flag (
                              id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                              org_id         UUID         NOT NULL REFERENCES organization (id),
    -- WHY CASCADE: if a transaction is ever deleted, its flags mean nothing anymore
                              transaction_id UUID         NOT NULL REFERENCES bank_transaction (id) ON DELETE CASCADE,
                              rule_type      VARCHAR(30)  NOT NULL,   -- no CHECK list: new rules get added, the Java enum guards it
                              severity       VARCHAR(10)  NOT NULL CHECK (severity IN ('CRITICAL', 'WARNING', 'INFO')),
                              status         VARCHAR(12)  NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'LEGITIMATE', 'DISPUTED')),
                              title          VARCHAR(100) NOT NULL,
                              explanation    VARCHAR(500) NOT NULL,
                              reviewed_by    UUID         REFERENCES app_user (id),
                              reviewed_at    TIMESTAMPTZ,
                              created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
                              updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- One flag per rule per transaction. Running detection twice can never double-flag.
CREATE UNIQUE INDEX uq_anomaly_flag_tx_rule ON anomaly_flag (transaction_id, rule_type);

-- For the Attention Queue: an org's flags by status, newest first
CREATE INDEX idx_anomaly_flag_org_status ON anomaly_flag (org_id, status, created_at DESC);

-- The "numbers behind the flag", in display order
CREATE TABLE anomaly_flag_evidence (
                                       flag_id    UUID         NOT NULL REFERENCES anomaly_flag (id) ON DELETE CASCADE,
                                       sort_order INT          NOT NULL,
                                       label      VARCHAR(50)  NOT NULL,
                                       detail     VARCHAR(100) NOT NULL,
                                       PRIMARY KEY (flag_id, sort_order)
);