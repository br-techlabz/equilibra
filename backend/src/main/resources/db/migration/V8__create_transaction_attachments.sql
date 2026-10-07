CREATE TABLE `transaction_attachments` (
    `id` CHAR(36) NOT NULL,
    `owner_id` CHAR(36) NOT NULL,
    `transaction_id` CHAR(36) NOT NULL,
    `original_file_name` VARCHAR(255) NOT NULL,
    `storage_key` VARCHAR(512) NOT NULL,
    `content_type` VARCHAR(100) NOT NULL,
    `size_bytes` BIGINT NOT NULL,
    `checksum_sha256` CHAR(64) NOT NULL,
    `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `version` BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_transaction_attachments_storage_key` (`storage_key`),
    CONSTRAINT `fk_transaction_attachments_owner` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_transaction_attachments_transaction` FOREIGN KEY (`transaction_id`) REFERENCES `financial_transactions` (`id`),
    KEY `idx_transaction_attachments_transaction` (`transaction_id`),
    KEY `idx_transaction_attachments_owner` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
