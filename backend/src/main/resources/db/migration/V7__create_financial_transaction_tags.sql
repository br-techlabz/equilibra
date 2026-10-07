CREATE TABLE `financial_transaction_tags` (
    `transaction_id` CHAR(36) NOT NULL,
    `tag_id` CHAR(36) NOT NULL,
    PRIMARY KEY (`transaction_id`, `tag_id`),
    CONSTRAINT `fk_transaction_tags_transaction` FOREIGN KEY (`transaction_id`) REFERENCES `financial_transactions` (`id`),
    CONSTRAINT `fk_transaction_tags_tag` FOREIGN KEY (`tag_id`) REFERENCES `tags` (`id`),
    KEY `idx_transaction_tags_tag` (`tag_id`),
    KEY `idx_transaction_tags_transaction` (`transaction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
