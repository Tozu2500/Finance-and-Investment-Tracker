

CREATE DATABASE IF NOT EXISTS finance_tracker CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE finance_tracker;

-- Users
CREATE TABLE IF NOT EXISTS users (
    id CHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL DEFAULT 'USER',
    timezone VARCHAR(50) NOT NULL DEFAULT 'UTC',
    avatar_url VARCHAR(500),
    last_login_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) Engine=InnoDB;

-- User settings
CREATE TABLE IF NOT EXISTS user_settings (
    id CHAR(36) NOT NULL PRIMARY KEY,
    user_id CHAR(36) NOT NULL UNIQUE,
    currency VARCHAR(10) NOT NULL DEFAULT 'USD',
    language VARCHAR(10) NOT NULL DEFAULT 'EN',
    dark_mode TINYINT(1) NOT NULL DEFAULT 0,
    font_scale DOUBLE NOT NULL DEFAULT 1.0,
    date_format VARCHAR(20) NOT NULL DEFAULT 'YYYY-MM-DD',
    week_start VARCHAR(20) NOT NULL DEFAULT 'Monday',
    compact_mode TINYINT(1) NOT NULL DEFAULT 0,
    budget_alerts TINYINT(1) NOT NULL DEFAULT 1,
    daily_summary TINYINT(1) NOT NULL DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) Engine=InnoDB;

-- Accounts
CREATE TABLE IF NOT EXISTS accounts (
    id CHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    icon VARCHAR(20),
    opening_balance DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
    account_type VARCHAR(20) NOT NULL DEFAULT 'CHECKING',
    color_hex VARCHAR(20),
    description VARCHAR(500),
    institution VARCHAR(100),
    credit_limit DECIMAL(19,4),
    is_archived TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    user_id CHAR(36) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_accounts_user (user_id),
    INDEX idx_accounts_user_active (user_id, is_archived)
) ENGINE=InnoDB;

