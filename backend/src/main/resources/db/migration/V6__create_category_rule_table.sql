CREATE TABLE category_rule (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id      UUID         NOT NULL REFERENCES organization (id),
    -- WHY ON DELETE CASCADE: if a category is ever deleted, its rules go with it
    category_id UUID         NOT NULL REFERENCES category (id) ON DELETE CASCADE,
    keyword     VARCHAR(100) NOT NULL CHECK (keyword = upper(keyword) AND length(keyword) >= 2),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- One keyword maps to exactly one category per org
CREATE UNIQUE INDEX uq_category_rule_org_keyword ON category_rule (org_id, keyword);

-- WHY: Postgres doesn't index foreign keys automatically. Without this,
-- deleting a category would scan the whole rule table to find its rules.
CREATE INDEX idx_category_rule_category_id ON category_rule (category_id);
