-- ============================================================
-- Equilibra - Migration V12: Regras de recorrência
-- ============================================================

CREATE TABLE `recurrence_rules` (
    `id` CHAR(36) NOT NULL COMMENT 'UUID da regra de recorrência',
    `owner_id` CHAR(36) NOT NULL COMMENT 'UUID do usuário proprietário',
    `type` VARCHAR(20) NOT NULL COMMENT 'Tipo da recorrência (EXPENSE, INCOME, TRANSFER)',
    `description` VARCHAR(255) NOT NULL COMMENT 'Descrição da recorrência',
    `amount` DECIMAL(19,2) NOT NULL COMMENT 'Valor em BRL',
    `category_id` CHAR(36) NOT NULL COMMENT 'UUID da categoria',
    `account_id` CHAR(36) NOT NULL COMMENT 'UUID da conta de ativo',
    `frequency` VARCHAR(20) NOT NULL COMMENT 'Frequência (DAILY, WEEKLY, MONTHLY, YEARLY)',
    `start_date` DATE NOT NULL COMMENT 'Data de início',
    `end_date` DATE COMMENT 'Data de fim (opcional)',
    `due_day` TINYINT NOT NULL COMMENT 'Dia do vencimento (1-31)',
    `status` VARCHAR(20) NOT NULL COMMENT 'Status (ACTIVE, INACTIVE)',
    `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    `version` BIGINT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    CONSTRAINT `ck_recurrence_amount` CHECK (`amount` > 0),
    CONSTRAINT `ck_recurrence_day` CHECK (`due_day` BETWEEN 1 AND 31),
    CONSTRAINT `ck_recurrence_type` CHECK (`type` IN ('EXPENSE', 'INCOME', 'TRANSFER')),
    CONSTRAINT `ck_recurrence_frequency` CHECK (`frequency` IN ('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY')),
    CONSTRAINT `ck_recurrence_status` CHECK (`status` IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT `fk_recurrence_category`
        FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`),
    CONSTRAINT `fk_recurrence_account`
        FOREIGN KEY (`account_id`) REFERENCES `asset_accounts` (`id`),
    CONSTRAINT `fk_recurrence_owner`
        FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Regras de recorrência financeira';

CREATE INDEX `idx_recurrence_owner_status` ON `recurrence_rules` (`owner_id`, `status`);
CREATE INDEX `idx_recurrence_owner_start` ON `recurrence_rules` (`owner_id`, `start_date`);