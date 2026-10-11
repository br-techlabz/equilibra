CREATE TABLE credit_card_installments (
 id CHAR(36) NOT NULL, owner_id CHAR(36) NOT NULL, purchase_id CHAR(36) NOT NULL, credit_card_id CHAR(36) NOT NULL,
 sequence_number INT NOT NULL, total_installments INT NOT NULL, principal_amount DECIMAL(19,2) NOT NULL, cycle_month DATE NOT NULL,
 PRIMARY KEY(id), CONSTRAINT fk_installment_owner FOREIGN KEY(owner_id) REFERENCES users(id), CONSTRAINT fk_installment_purchase FOREIGN KEY(purchase_id) REFERENCES credit_card_purchases(id), CONSTRAINT fk_installment_card FOREIGN KEY(credit_card_id) REFERENCES credit_cards(id),
 CONSTRAINT uq_installment_purchase_sequence UNIQUE(purchase_id,sequence_number), CONSTRAINT ck_installment_sequence CHECK(sequence_number>0 AND total_installments>0 AND sequence_number<=total_installments), CONSTRAINT ck_installment_amount CHECK(principal_amount>0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_installment_owner_card_cycle ON credit_card_installments(owner_id,credit_card_id,cycle_month);
CREATE INDEX idx_installment_owner_status_source ON credit_card_installments(owner_id,purchase_id);
