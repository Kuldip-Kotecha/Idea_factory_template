-- =============================================================================
-- Database Initialization Script for App Template
-- Creates microservice databases and assigns dedicated credentials.
-- =============================================================================

-- 1. User / Authentication Service Database
CREATE DATABASE IF NOT EXISTS app_users;
CREATE USER IF NOT EXISTS 'auth_user'@'%' IDENTIFIED BY 'auth_password';
GRANT ALL PRIVILEGES ON app_users.* TO 'auth_user'@'%';

-- 2. Core Business Features Service Database
CREATE DATABASE IF NOT EXISTS app_core;
CREATE USER IF NOT EXISTS 'core_user'@'%' IDENTIFIED BY 'core_password';
GRANT ALL PRIVILEGES ON app_core.* TO 'core_user'@'%';

-- 3. Media / Asset Storage Service Database (Optional)
CREATE DATABASE IF NOT EXISTS app_media;
CREATE USER IF NOT EXISTS 'media_user'@'%' IDENTIFIED BY 'media_password';
GRANT ALL PRIVILEGES ON app_media.* TO 'media_user'@'%';

FLUSH PRIVILEGES;
