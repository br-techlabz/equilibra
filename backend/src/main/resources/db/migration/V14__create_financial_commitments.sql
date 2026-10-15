-- ============================================================
-- Equilibra - Migration V14: Compromissos financeiros
-- ============================================================

CREATE TABLE `financial_commitments` (
    `id` CHAR(36) NOT NULL COMMENT 'UUID do compromisso financeiro',
    `owner_id` CHAR(36) NOT NULL COMMENT 'UUID do usuário proprietário',
    `recurrence_rule_id` CHAR(36) COMMENT 'UUID da regra de recorrência (opcional)',
    `type` VARCHAR(20) NOT NULL COMMENT 'Tipo (EXPENSE, INCOME, TRANSFER)',
    `description` VARCHAR(255) NOT NULL COMMENT 'Descrição do compromisso',
    `planned_amount` DECIMAL(19,2) NOT NULL COMMENT 'Valor planejado em BRL',
    `due_date` DATE NOT NULL COMMENT 'Data de vencimento',
    `account_id` CHAR(36) NOT NULL COMMENT 'UUID da conta de ativo',
    `category_id` CHAR(36) NOT NULL COMMENT 'UUID da categoria',
    `status` VARCHAR(20) NOT NULL COMMENT 'Status (PENDING, SETTLED, CANCELLED)',
    `financial_transaction_id` CHAR(36) COMMENT 'UUID da transação financeira vinculada',
    `settled_at` TIMESTAMP(6) COMMENT 'Timestamp de liquidação',
    `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    `version` BIGINT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    CONSTRAINT `ck_commitment_amount` CHECK (`planned_amount` > 0),
    CONSTRAINT `ck_commitment_status` CHECK (`status` IN ('PENDING', 'SETTLED', 'CANCELLED')),
    CONSTRAINT `ck_commitment_type` CHECK (`type` IN ('EXPENSE', 'INCOME', 'TRANSFER')),
    CONSTRAINT `uq_commitment_tx` UNIQUE (`financial_transaction_id`),
    CONSTRAINT `uq_commitment_occurrence` UNIQUE (`owner_id`, `recurrence_rule_id`, `due_date`),
    CONSTRAINT `fk_commitment_rule`
        FOREIGN KEY (`recurrence_rule_id`) REFERENCES `recurrence_rules` (`id`),
    CONSTRAINT `fk_commitment_account`
        FOREIGN KEY (`account_id`) REFERENCES `asset_accounts` (`id`),
    CONSTRAINT `fk_commitment_category`
        FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`),
    CONSTRAINT `fk_commitment_owner`
        FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Compromissos financeiros recorrentes e agendados';

CREATE INDEX `idx_commitment_owner_due` ON `financial_commitments` (`owner_id`, `due_date`);
CREATE INDEX `idx_commitment_owner_status` ON `financial_commitments` (`owner_id`, `status`);