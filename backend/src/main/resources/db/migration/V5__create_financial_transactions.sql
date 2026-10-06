CREATE TABLE `financial_transactions` (
    `id` CHAR(36) NOT NULL,
    `owner_id` CHAR(36) NOT NULL,
    `type` VARCHAR(20) NOT NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    `description` VARCHAR(255) NOT NULL,
    `amount` DECIMAL(19,2) NOT NULL,
    `occurred_at` TIMESTAMP(6) NOT NULL,
    `category_id` CHAR(36) NOT NULL,
    `source_account_id` CHAR(36),
    `destination_account_id` CHAR(36),
    `notes` VARCHAR(4000),
    `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    `cancelled_at` TIMESTAMP(6),
    `version` BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_financial_transactions_owner`
        FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_financial_transactions_category`
        FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`),
    CONSTRAINT `fk_financial_transactions_source_account`
        FOREIGN KEY (`source_account_id`) REFERENCES `asset_accounts` (`id`),
    CONSTRAINT `fk_financial_transactions_destination_account`
        FOREIGN KEY (`destination_account_id`) REFERENCES `asset_accounts` (`id`),
    CONSTRAINT `ck_financial_transactions_type`
        CHECK (`type` IN ('EXPENSE', 'INCOME', 'TRANSFER')),
    CONSTRAINT `ck_financial_transactions_status`
        CHECK (`status` IN ('ACTIVE', 'CANCELLED')),
    CONSTRAINT `ck_financial_transactions_amount`
        CHECK (`amount` > 0),
    CONSTRAINT `ck_financial_transactions_accounts`
        CHECK (
            (`type` = 'EXPENSE' AND `source_account_id` IS NOT NULL AND `destination_account_id` IS NULL)
            OR (`type` = 'INCOME' AND `source_account_id` IS NULL AND `destination_account_id` IS NOT NULL)
            OR (`type` = 'TRANSFER' AND `source_account_id` IS NOT NULL AND `destination_account_id` IS NOT NULL AND `source_account_id` <> `destination_account_id`)
        ),
    CONSTRAINT `ck_financial_transactions_cancelled_at`
        CHECK ((`status` = 'ACTIVE' AND `cancelled_at` IS NULL) OR (`status` = 'CANCELLED' AND `cancelled_at` IS NOT NULL)),
    KEY `idx_financial_transactions_owner_occurred` (`owner_id`, `occurred_at`),
    KEY `idx_financial_transactions_owner_status_occurred` (`owner_id`, `status`, `occurred_at`),
    KEY `idx_financial_transactions_owner_type_occurred` (`owner_id`, `type`, `occurred_at`),
    KEY `idx_financial_transactions_owner_source_occurred` (`owner_id`, `source_account_id`, `occurred_at`),
    KEY `idx_financial_transactions_owner_destination_occurred` (`owner_id`, `destination_account_id`, `occurred_at`),
    KEY `idx_financial_transactions_owner_category_occurred` (`owner_id`, `category_id`, `occurred_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
