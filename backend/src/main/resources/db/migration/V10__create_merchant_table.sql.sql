CREATE TABLE merchant (
                          id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
                          org_id        UUID         NOT NULL REFERENCES organization (id),
                          name          VARCHAR(100) NOT NULL,                                 -- cleaned, e.g. "SPAR LANGEBAAN"
                          category_id   UUID         REFERENCES category (id) ON DELETE SET NULL, -- "remembered" category
                          first_seen_on DATE         NOT NULL,                                 -- used later by the new-merchant rule
                          created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
                          updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_merchant_org_name ON merchant (org_id, name);