-- Схема БД для Unit Converter Pro (Этап 6)
-- Вариант 2-2: ownerId (число) + один DataAccess класс
-- Применить через pgAdmin Query Tool или IntelliJ Database Console

-- ===== Таблица пользователей =====
CREATE TABLE users (
    id              SERIAL          PRIMARY KEY,
    login           VARCHAR(64)     NOT NULL UNIQUE,
    password_hash   CHAR(64)        NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ===== Таблица единиц измерения =====
CREATE TABLE units (
    id              SERIAL          PRIMARY KEY,
    code            VARCHAR(16)     NOT NULL,
    name            VARCHAR(64)     NOT NULL,
    owner_id        INT             NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ===== Таблица правил конверсии =====
CREATE TABLE conversion_rules (
    id              SERIAL          PRIMARY KEY,
    from_unit_code  VARCHAR(16)     NOT NULL,
    to_unit_code    VARCHAR(16)     NOT NULL,
    factor          DOUBLE PRECISION NOT NULL CHECK (factor > 0),
    owner_id        INT             NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ===== Индексы для ускорения частых запросов =====
CREATE INDEX idx_units_owner ON units(owner_id);
CREATE INDEX idx_rules_owner ON conversion_rules(owner_id);
CREATE INDEX idx_rules_from_code ON conversion_rules(from_unit_code);
