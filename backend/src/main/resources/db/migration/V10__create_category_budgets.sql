CREATE TABLE category_budgets (
    id VARCHAR(36) NOT NULL,
    owner_id VARCHAR(36) NOT NULL,
    category_id VARCHAR(36) NOT NULL,
    year SMALLINT NOT NULL,
    month TINYINT NOT NULL,
    planned_amount DECIMAL(19,2) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    version BIGINT NULL,
    CONSTRAINT pk_category_budgets PRIMARY KEY (id),
    CONSTRAINT ck_category_budgets_month CHECK (month BETWEEN 1 AND 12),
    CONSTRAINT ck_category_budgets_amount CHECK (planned_amount >= 0),
    CONSTRAINT uq_category_budgets_owner_category_month UNIQUE (owner_id, category_id, year, month),
    CONSTRAINT fk_category_budgets_category FOREIGN KEY (category_id) REFERENCES categories(id)
);
CREATE INDEX idx_category_budgets_owner_period ON category_budgets(owner_id, year, month);
CREATE INDEX idx_category_budgets_category ON category_budgets(category_id);
