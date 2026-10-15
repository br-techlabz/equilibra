-- ============================================================
-- Equilibra - Migration V23: Adicionar timestamp de compra
-- ============================================================

ALTER TABLE `credit_card_purchases` ADD COLUMN `purchase_at` TIMESTAMP(6) NULL COMMENT 'Timestamp da compra em UTC';

UPDATE `credit_card_purchases` SET `purchase_at` = CONVERT_TZ(CAST(`purchase_date` AS DATETIME), 'America/Sao_Paulo', 'UTC') WHERE `purchase_at` IS NULL;

CREATE INDEX `idx_purchase_owner_card_time` ON `credit_card_purchases` (`owner_id`, `credit_card_id`, `purchase_at`);