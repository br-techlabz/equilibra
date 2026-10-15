-- ============================================================
-- Equilibra - Migration V18: Parcelas de cartão de crédito
-- ============================================================

CREATE TABLE `credit_card_installments` (
    `id` CHAR(36) NOT NULL COMMENT 'UUID da parcela',
    `owner_id` CHAR(36) NOT NULL COMMENT 'UUID do usuário proprietário',
    `purchase_id` CHAR(36) NOT NULL COMMENT 'UUID da compra',
    `credit_card_id` CHAR(36) NOT NULL COMMENT 'UUID do cartão de crédito',
    `sequence_number` INT NOT NULL COMMENT 'Número da parcela (1...N)',
    `total_installments` INT NOT NULL COMMENT 'Total de parcelas',
    `principal_amount` DECIMAL(19,2) NOT NULL COMMENT 'Valor principal da parcela em BRL',
    `cycle_month` DATE NOT NULL COMMENT 'Mês do ciclo da fatura',
    `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    `version` BIGINT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_installment_owner`
        FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_installment_purchase`
        FOREIGN KEY (`purchase_id`) REFERENCES `credit_card_purchases` (`id`),
    CONSTRAINT `fk_installment_card`
        FOREIGN KEY (`credit_card_id`) REFERENCES `credit_cards` (`id`),
    CONSTRAINT `uq_installment_purchase_sequence` UNIQUE (`purchase_id`, `sequence_number`),
    CONSTRAINT `ck_installment_sequence` CHECK (`sequence_number` > 0 AND `total_installments` > 0 AND `sequence_number` <= `total_installments`),
    CONSTRAINT `ck_installment_amount` CHECK (`principal_amount` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Parcelas das compras de cartão de crédito';

CREATE INDEX `idx_installment_owner_card_cycle` ON `credit_card_installments` (`owner_id`, `credit_card_id`, `cycle_month`);
CREATE INDEX `idx_installment_owner_purchase` ON `credit_card_installments` (`owner_id`, `purchase_id`);