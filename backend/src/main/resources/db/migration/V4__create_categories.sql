-- Categorias privadas, com unicidade somente entre nomes ativos do mesmo owner.
CREATE TABLE `categories` (
    `id` CHAR(36) NOT NULL,
    `owner_id` CHAR(36) NOT NULL,
    `name` VARCHAR(100) NOT NULL,
    `normalized_name` VARCHAR(100) NOT NULL,
    `applicability` VARCHAR(20) NOT NULL,
    `active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    `version` BIGINT NOT NULL DEFAULT 0,
    `active_normalized_name` VARCHAR(100)
        GENERATED ALWAYS AS (CASE WHEN `active` THEN `normalized_name` ELSE NULL END) STORED,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_categories_owner`
        FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`),
    CONSTRAINT `ck_categories_applicability`
        CHECK (`applicability` IN ('EXPENSE', 'INCOME', 'BOTH')),
    CONSTRAINT `uk_categories_owner_active_name`
        UNIQUE (`owner_id`, `active_normalized_name`),
    KEY `idx_categories_owner_active_applicability` (`owner_id`, `active`, `applicability`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
