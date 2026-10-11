CREATE TABLE credit_cards (
    id CHAR(36) NOT NULL,
    owner_id CHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    institution VARCHAR(100) NOT NULL,
    nickname VARCHAR(100),
    total_limit DECIMAL(19,2) NOT NULL,
    closing_day TINYINT NOT NULL,
    due_day TINYINT NOT NULL,
    timezone VARCHAR(64) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_credit_cards_owner FOREIGN KEY (owner_id) REFERENCES users(id),
    CONSTRAINT ck_credit_cards_limit CHECK (total_limit > 0),
    CONSTRAINT ck_credit_cards_closing_day CHECK (closing_day BETWEEN 1 AND 31),
    CONSTRAINT ck_credit_cards_due_day CHECK (due_day BETWEEN 1 AND 31),
    CONSTRAINT ck_credit_cards_status CHECK (status IN ('ACTIVE', 'INACTIVE') )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_credit_cards_owner_status ON credit_cards(owner_id, status);
CREATE INDEX idx_credit_cards_owner_name ON credit_cards(owner_id, name);
