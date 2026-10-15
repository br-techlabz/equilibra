-- ============================================================
-- Equilibra - Migration V10: Orçamentos por categoria
-- ============================================================

CREATE TABLE `category_budgets` (
    `id` CHAR(36) NOT NULL COMMENT 'UUID do orçamento',
    `owner_id` CHAR(36) NOT NULL COMMENT 'UUID do usuário proprietário',
    `category_id` CHAR(36) NOT NULL COMMENT 'UUID da categoria',
    `year` SMALLINT NOT NULL COMMENT 'Ano do orçamento',
    `month` TINYINT NOT NULL COMMENT 'Mês do orçamento (1-12)',
    `planned_amount` DECIMAL(19,2) NOT NULL COMMENT 'Valor planejado em BRL',
    `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    `version` BIGINT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    CONSTRAINT `ck_category_budgets_month` CHECK (`month` BETWEEN 1 AND 12),
    CONSTRAINT `ck_category_budgets_amount` CHECK (`planned_amount` >= 0),
    CONSTRAINT `uq_category_budgets_owner_category_month` UNIQUE (`owner_id`, `category_id`, `year`, `month`),
    CONSTRAINT `fk_category_budgets_category`
        FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`),
    CONSTRAINT `fk_category_budgets_owner`
        FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Orçamentos por categoria e período';

CREATE INDEX `idx_category_budgets_owner_period` ON `category_budgets` (`owner_id`, `year`, `month`);
CREATE INDEX `idx_category_budgets_category` ON `category_budgets` (`category_id`);