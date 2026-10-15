-- ============================================================
-- Equilibra - Migration V20: Vincular parcelas a faturas
-- ============================================================

ALTER TABLE `credit_card_installments` ADD COLUMN `statement_id` CHAR(36) NULL COMMENT 'UUID da fatura vinculada';
ALTER TABLE `credit_card_installments` ADD CONSTRAINT `fk_installment_statement`
    FOREIGN KEY (`statement_id`) REFERENCES `credit_card_statements` (`id`);
CREATE INDEX `idx_installment_owner_statement_sequence` ON `credit_card_installments` (`owner_id`, `statement_id`, `sequence_number`);