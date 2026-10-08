CREATE TABLE `refresh_sessions` (
    `id` CHAR(36) NOT NULL,
    `owner_id` CHAR(36) NOT NULL,
    `token_hash` CHAR(64) NOT NULL,
    `expires_at` TIMESTAMP(6) NOT NULL,
    `revoked_at` TIMESTAMP(6),
    `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `rotated_at` TIMESTAMP(6),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_refresh_sessions_token_hash` (`token_hash`),
    CONSTRAINT `fk_refresh_sessions_owner` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`),
    KEY `idx_refresh_sessions_owner` (`owner_id`),
    KEY `idx_refresh_sessions_expires` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
