-- ============================================================
-- Equilibra - Migration V21: Suporte a transações de pagamento de fatura
-- ============================================================

ALTER TABLE `financial_transactions` MODIFY COLUMN `category_id` CHAR(36) NULL;
ALTER TABLE `financial_transactions` DROP CHECK `ck_financial_transactions_type`;
ALTER TABLE `financial_transactions` ADD CONSTRAINT `ck_financial_transactions_type`
    CHECK (`type` IN ('EXPENSE', 'INCOME', 'TRANSFER', 'STATEMENT_PAYMENT'));
ALTER TABLE `financial_transactions` DROP CHECK `ck_financial_transactions_accounts`;
ALTER TABLE `financial_transactions` ADD CONSTRAINT `ck_financial_transactions_accounts` CHECK (
    (`type` = 'EXPENSE' AND `source_account_id` IS NOT NULL AND `destination_account_id` IS NULL AND `category_id` IS NOT NULL)
    OR (`type` = 'INCOME' AND `source_account_id` IS NULL AND `destination_account_id` IS NOT NULL AND `category_id` IS NOT NULL)
    OR (`type` = 'TRANSFER' AND `source_account_id` IS NOT NULL AND `destination_account_id` IS NOT NULL AND `source_account_id` <> `destination_account_id` AND `category_id` IS NOT NULL)
    OR (`type` = 'STATEMENT_PAYMENT' AND `source_account_id` IS NOT NULL AND `destination_account_id` IS NULL AND `category_id` IS NULL)
);