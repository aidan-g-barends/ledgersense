-- WHY a new migration instead of editing V8: V8 already ran. Applied migrations are never edited.
ALTER TABLE bank_transaction
    ADD COLUMN merchant_id UUID REFERENCES merchant (id) ON DELETE SET NULL;

CREATE INDEX idx_bank_transaction_merchant ON bank_transaction (merchant_id);