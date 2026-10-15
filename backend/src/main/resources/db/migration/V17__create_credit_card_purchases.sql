-- ============================================================
-- Equilibra - Migration V17: Compras de cartão de crédito
-- ============================================================

CREATE TABLE `credit_card_purchases` (
    `id` CHAR(36) NOT NULL COMMENT 'UUID da compra',
    `owner_id` CHAR(36) NOT NULL COMMENT 'UUID do usuário proprietário',
    `credit_card_id` CHAR(36) NOT NULL COMMENT 'UUID do cartão de crédito',
    `category_id` CHAR(36) NOT NULL COMMENT 'UUID da categoria',
    `description` VARCHAR(255) NOT NULL COMMENT 'Descrição da compra',
    `total_amount` DECIMAL(19,2) NOT NULL COMMENT 'Valor total em BRL',
    `purchase_date` DATE NOT NULL COMMENT 'Data da compra',
    `installment_count` INT NOT NULL COMMENT 'Número de parcelas',
    `cycle_month` DATE NOT NULL COMMENT 'Mês do ciclo da fatura',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Status (ACTIVE, REVERSED, CANCELLED)',
    `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    `version` BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_purchase_owner`
        FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_purchase_card`
        FOREIGN KEY (`credit_card_id`) REFERENCES `credit_cards` (`id`),
    CONSTRAINT `fk_purchase_category`
        FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`),
    CONSTRAINT `ck_purchase_amount` CHECK (`total_amount` > 0),
    CONSTRAINT `ck_purchase_installments` CHECK (`installment_count` > 0),
    CONSTRAINT `ck_purchase_status` CHECK (`status` IN ('ACTIVE', 'REVERSED', 'CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Compras realizadas no cartão de crédito';

CREATE INDEX `idx_purchase_owner_card_date` ON `credit_card_purchases` (`owner_id`, `credit_card_id`, `purchase_date`);
CREATE INDEX `idx_purchase_owner_status` ON `credit_card_purchases` (`owner_id`, `status`);