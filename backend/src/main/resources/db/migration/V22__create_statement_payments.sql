-- ============================================================
-- Equilibra - Migration V22: Pagamentos de fatura
-- ============================================================

CREATE TABLE `statement_payments` (
    `id` CHAR(36) NOT NULL COMMENT 'UUID do pagamento de fatura',
    `owner_id` CHAR(36) NOT NULL COMMENT 'UUID do usuário proprietário',
    `statement_id` CHAR(36) NOT NULL COMMENT 'UUID da fatura',
    `bank_account_id` CHAR(36) NOT NULL COMMENT 'UUID da conta bancária de origem',
    `amount` DECIMAL(19,2) NOT NULL COMMENT 'Valor do pagamento em BRL',
    `paid_at` TIMESTAMP(6) NOT NULL COMMENT 'Timestamp do pagamento',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Status (ACTIVE, REVERSED)',
    `financial_transaction_id` CHAR(36) NOT NULL COMMENT 'UUID da transação financeira vinculada',
    `idempotency_key` VARCHAR(100) COMMENT 'Chave de idempotência',
    `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    `version` BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_payment_owner`
        FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_payment_statement`
        FOREIGN KEY (`statement_id`) REFERENCES `credit_card_statements` (`id`),
    CONSTRAINT `fk_payment_account`
        FOREIGN KEY (`bank_account_id`) REFERENCES `asset_accounts` (`id`),
    CONSTRAINT `fk_payment_transaction`
        FOREIGN KEY (`financial_transaction_id`) REFERENCES `financial_transactions` (`id`),
    CONSTRAINT `uq_payment_transaction` UNIQUE (`financial_transaction_id`),
    CONSTRAINT `uq_payment_idempotency` UNIQUE (`owner_id`, `idempotency_key`),
    CONSTRAINT `ck_payment_amount` CHECK (`amount` > 0),
    CONSTRAINT `ck_payment_status` CHECK (`status` IN ('ACTIVE', 'REVERSED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Pagamentos de fatura de cartão de crédito';

CREATE INDEX `idx_payment_owner_statement` ON `statement_payments` (`owner_id`, `statement_id`, `paid_at`);