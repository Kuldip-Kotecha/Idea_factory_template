-- =============================================================================
-- Schema: Core Business Service Database (app_core)
-- Starter schema demonstrating entity definition for new business modules.
-- =============================================================================

USE app_core;

CREATE TABLE IF NOT EXISTS sample_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT 'Owner user ID from user-service',
    title VARCHAR(200) NOT NULL,
    description TEXT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE, ARCHIVED, DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_sample_items_user_id (user_id),
    INDEX idx_sample_items_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
