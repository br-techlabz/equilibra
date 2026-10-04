-- ============================================================
-- Equilibra - Migration V2: Criação da tabela de usuários
-- ============================================================
-- Cria a tabela de usuários com campos essenciais de identidade e autenticação.
-- ============================================================

CREATE TABLE `users` (
    `id` CHAR(36) NOT NULL COMMENT 'UUID do usuário',
    `email` VARCHAR(255) NOT NULL COMMENT 'Email do usuário (normalizado: trim + lowercase)',
    `password_hash` VARCHAR(255) NOT NULL COMMENT 'Hash da senha (BCrypt)',
    `active` BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Status da conta (ativo/inativo)',
    `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Data/hora de criação (UTC)',
    `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'Data/hora da última atualização (UTC)',
    `version` BIGINT NOT NULL DEFAULT 0 COMMENT 'Versão para optimistic locking',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_users_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Tabela de usuários do Equilibra';