-- =============================================================================
-- Schema: User Service Database (app_users)
-- Generic User Management and Authentication Schema
-- =============================================================================

USE app_users;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(200) NOT NULL UNIQUE,
    picture VARCHAR(255) NULL,
    provider VARCHAR(50) NULL COMMENT 'google, local, etc.',
    provider_id VARCHAR(100) NULL,
    password_hash VARCHAR(255) NULL,
    email_verified BIT NOT NULL DEFAULT 0,
    verification_token VARCHAR(64) NULL,
    verification_token_expires_at DATETIME(6) NULL,
    password_reset_token VARCHAR(64) NULL,
    password_reset_token_expires_at DATETIME(6) NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT 'USER, ADMIN',
    active BIT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_users_email (email),
    INDEX idx_users_verification_token (verification_token),
    INDEX idx_users_password_reset_token (password_reset_token),
    INDEX idx_users_role (role),
    INDEX idx_users_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
