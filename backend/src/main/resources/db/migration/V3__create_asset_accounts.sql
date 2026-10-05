-- ============================================================
-- Equilibra - Migration V3: Contas de ativo
-- ============================================================

CREATE TABLE `asset_accounts` (
    `id` CHAR(36) NOT NULL COMMENT 'UUID da conta de ativo',
    `owner_id` CHAR(36) NOT NULL COMMENT 'UUID do usuário proprietário',
    `name` VARCHAR(100) NOT NULL COMMENT 'Nome exibido da conta',
    `normalized_name` VARCHAR(100) NOT NULL COMMENT 'Nome normalizado para unicidade por usuário',
    `type` VARCHAR(20) NOT NULL COMMENT 'Tipo da conta de ativo',
    `initial_balance` DECIMAL(19,2) NOT NULL COMMENT 'Saldo inicial em BRL',
    `active` BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Indica se a conta está ativa',
    `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    `version` BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_asset_accounts_owner`
        FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`),
    CONSTRAINT `ck_asset_accounts_type`
        CHECK (`type` IN ('CHECKING', 'SAVINGS', 'CASH', 'INVESTMENT', 'DIGITAL', 'OTHER')),
    CONSTRAINT `uk_asset_accounts_owner_name_active`
        UNIQUE (`owner_id`, `normalized_name`, `active`),
    KEY `idx_asset_accounts_owner` (`owner_id`),
    KEY `idx_asset_accounts_owner_active` (`owner_id`, `active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Contas de ativo do Equilibra';
