-- ============================================================
-- Equilibra - Migration V24: Estender status de fatura
-- ============================================================

ALTER TABLE `credit_card_statements` DROP CHECK `ck_statement_status`;
ALTER TABLE `credit_card_statements` ADD CONSTRAINT `ck_statement_status`
    CHECK (`status` IN ('OPEN', 'CLOSED', 'OVERDUE', 'PARTIALLY_PAID', 'PAID', 'CANCELLED'));