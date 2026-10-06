CREATE TABLE `tags` (
    `id` CHAR(36) NOT NULL,
    `owner_id` CHAR(36) NOT NULL,
    `name` VARCHAR(100) NOT NULL,
    `normalized_name` VARCHAR(100) NOT NULL,
    `active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    `version` BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_tags_owner` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`),
    KEY `idx_tags_owner_active_name` (`owner_id`, `active`, `normalized_name`),
    KEY `idx_tags_owner_name` (`owner_id`, `normalized_name`),
    `active_owner_name_key` VARCHAR(133) GENERATED ALWAYS AS (
        CASE WHEN `active` THEN CONCAT(`owner_id`, ':', `normalized_name`) ELSE NULL END
    ) STORED,
    UNIQUE KEY `uk_tags_owner_active_name` (`active_owner_name_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
