CREATE TABLE refresh_token (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    -- ON DELETE CASCADE: deleting a user deletes their tokens too
    user_id      UUID         NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    -- SHA-256 of the token = always exactly 64 hex characters. Never the raw token.
    token_hash   CHAR(64)     NOT NULL,
    expires_at   TIMESTAMPTZ  NOT NULL,
    -- NULL = still active. A date = revoked (used, or logged out)
    revoked_at   TIMESTAMPTZ,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Every refresh looks up by hash, so it must be unique AND fast
CREATE UNIQUE INDEX uq_refresh_token_hash ON refresh_token (token_hash);
CREATE INDEX idx_refresh_token_user_id ON refresh_token (user_id);
