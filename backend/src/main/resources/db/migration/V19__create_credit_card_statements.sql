-- ============================================================
-- Equilibra - Migration V19: Faturas de cartão de crédito
-- ============================================================

CREATE TABLE `credit_card_statements` (
    `id` CHAR(36) NOT NULL COMMENT 'UUID da fatura',
    `owner_id` CHAR(36) NOT NULL COMMENT 'UUID do usuário proprietário',
    `credit_card_id` CHAR(36) NOT NULL COMMENT 'UUID do cartão de crédito',
    `cycle_start` DATE NOT NULL COMMENT 'Início do ciclo de fatura',
    `cycle_end` DATE NOT NULL COMMENT 'Fim do ciclo de fatura',
    `closing_at` TIMESTAMP(6) NOT NULL COMMENT 'Timestamp de fechamento da fatura',
    `due_date` DATE NOT NULL COMMENT 'Data de vencimento',
    `status` VARCHAR(20) NOT NULL DEFAULT 'OPEN' COMMENT 'Status (OPEN, CLOSED, OVERDUE, CANCELLED)',
    `total_amount` DECIMAL(19,2) NOT NULL DEFAULT '0.00' COMMENT 'Valor total da fatura em BRL',
    `paid_amount` DECIMAL(19,2) NOT NULL DEFAULT '0.00' COMMENT 'Valor pago em BRL',
    `outstanding_amount` DECIMAL(19,2) NOT NULL DEFAULT '0.00' COMMENT 'Valor em aberto em BRL',
    `closed_at` TIMESTAMP(6) COMMENT 'Timestamp de fechamento efetivo',
    `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    `version` BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_statement_owner`
        FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_statement_card`
        FOREIGN KEY (`credit_card_id`) REFERENCES `credit_cards` (`id`),
    CONSTRAINT `uq_statement_cycle` UNIQUE (`owner_id`, `credit_card_id`, `cycle_start`, `cycle_end`),
    CONSTRAINT `ck_statement_status` CHECK (`status` IN ('OPEN', 'CLOSED', 'OVERDUE', 'CANCELLED')),
    CONSTRAINT `ck_statement_amounts` CHECK (`total_amount` >= 0 AND `paid_amount` >= 0 AND `outstanding_amount` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Faturas de cartão de crédito';

CREATE INDEX `idx_statement_owner_card_cycle` ON `credit_card_statements` (`owner_id`, `credit_card_id`, `cycle_start`);
CREATE INDEX `idx_statement_owner_status` ON `credit_card_statements` (`owner_id`, `status`);