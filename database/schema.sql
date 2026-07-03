-- Finance Enhancement Application 1.0
-- MySQL8.0 4.7.2026

CREATE DATABASE IF NOT EXISTS finance_tracker CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE finance_tracker;

-- Users 
CREATE TABLE IF NOT EXISTS users (
    id             VARCHAR(255)  NOT NULL PRIMARY KEY,
    name           VARCHAR(255)  NOT NULL,
    email          VARCHAR(255)  NOT NULL,
    password       VARCHAR(255)  NOT NULL,
    role           ENUM('ADMIN','USER') NOT NULL,
    timezone       VARCHAR(50)   NOT NULL,
    avatar_url     VARCHAR(500),
    last_login_at  DATETIME(6),
    created_at     DATETIME(6),
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- User settings 
CREATE TABLE IF NOT EXISTS user_settings (
    id                    VARCHAR(255) NOT NULL PRIMARY KEY,
    user_id               VARCHAR(255) NOT NULL,
    currency              VARCHAR(255),
    language              VARCHAR(255),
    dark_mode             BIT(1),
    font_scale            DOUBLE,
    date_format           VARCHAR(255),
    week_start            VARCHAR(255),
    compact_mode          BIT(1),
    budget_alerts         BIT(1),
    daily_summary         BIT(1),
    default_account_id    VARCHAR(36),
    notifications_enabled TINYINT(1)   NOT NULL DEFAULT 1,
    show_cents            TINYINT(1)   NOT NULL DEFAULT 1,
    UNIQUE KEY uk_settings_user (user_id),
    CONSTRAINT fk_settings_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Accounts 
CREATE TABLE IF NOT EXISTS accounts (
    id              VARCHAR(255)  NOT NULL PRIMARY KEY,
    name            VARCHAR(255)  NOT NULL,
    icon            VARCHAR(255),
    opening_balance DECIMAL(19,4) NOT NULL,
    account_type    VARCHAR(255)  NOT NULL,
    color_hex       VARCHAR(255),
    description     VARCHAR(500),
    institution     VARCHAR(100),
    credit_limit    DECIMAL(19,4),
    is_archived     BIT(1)        NOT NULL,
    is_default      TINYINT(1)    NOT NULL DEFAULT 0,
    sort_order      INT           NOT NULL DEFAULT 0,
    created_at      DATETIME(6),
    updated_at      DATETIME(6),
    user_id         VARCHAR(255)  NOT NULL,
    INDEX idx_accounts_user        (user_id),
    INDEX idx_accounts_user_active (user_id, is_archived),
    CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Categories
CREATE TABLE IF NOT EXISTS categories (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type ENUM('EXPENSE', 'INCOME') NOT NULL,
    icon VARCHAR(255),
    color_hex VARCHAR(255),
    monthly_budget DECIMAL(19,4) NOT NULL,
    description    VARCHAR(500),
    sort_order     INT,
    parent_id      VARCHAR(255),
    is_archived    BIT(1)        NOT NULL,
    created_at     DATETIME(6),
    updated_at     DATETIME(6),
    user_id        VARCHAR(255)  NOT NULL,
    INDEX idx_categories_user_type   (user_id, type),
    INDEX idx_categories_user_active (user_id, is_archived),
    INDEX idx_categories_parent      (parent_id),
    CONSTRAINT fk_categories_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tags
CREATE TABLE IF NOT EXISTS tags (
    id        VARCHAR(255) NOT NULL PRIMARY KEY,
    name      VARCHAR(50)  NOT NULL,
    color_hex VARCHAR(20)  NOT NULL,
    user_id   VARCHAR(255) NOT NULL,
    INDEX idx_tags_user (user_id),
    CONSTRAINT fk_tags_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;