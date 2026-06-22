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
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- Один владелец не может завести две единицы с одинаковым кодом
    CONSTRAINT units_code_owner_unique UNIQUE (code, owner_id)
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

-- =====================================================================
-- Доп. задание (вариант 3): real-time синхронизация через LISTEN/NOTIFY
-- =====================================================================
-- Идея: при любом изменении в таблицах БД сама шлёт уведомление в канал
-- 'lab_changes'. Запущенные клиенты подписаны на этот канал (LISTEN) и,
-- получив сигнал, перечитывают данные — без ручного Refresh.
--
-- Этот блок можно применять повторно (DROP ... IF EXISTS + CREATE OR REPLACE).

-- Триггерная функция: шлёт уведомление с payload "имя_таблицы:операция".
CREATE OR REPLACE FUNCTION notify_lab_changes() RETURNS trigger AS $$
BEGIN
    -- pg_notify(канал, текст). TG_TABLE_NAME = имя таблицы, TG_OP = INSERT/UPDATE/DELETE.
    PERFORM pg_notify('lab_changes', TG_TABLE_NAME || ':' || TG_OP);
    RETURN NULL;   -- AFTER STATEMENT-триггер: возвращаемое значение не используется
END;
$$ LANGUAGE plpgsql;

-- Вешаем триггер на каждую таблицу. FOR EACH STATEMENT — одно уведомление
-- на одну команду (нам важен только факт изменения, а не каждая строка).
DROP TRIGGER IF EXISTS trg_users_notify ON users;
CREATE TRIGGER trg_users_notify
    AFTER INSERT OR UPDATE OR DELETE ON users
    FOR EACH STATEMENT EXECUTE FUNCTION notify_lab_changes();

DROP TRIGGER IF EXISTS trg_units_notify ON units;
CREATE TRIGGER trg_units_notify
    AFTER INSERT OR UPDATE OR DELETE ON units
    FOR EACH STATEMENT EXECUTE FUNCTION notify_lab_changes();

DROP TRIGGER IF EXISTS trg_rules_notify ON conversion_rules;
CREATE TRIGGER trg_rules_notify
    AFTER INSERT OR UPDATE OR DELETE ON conversion_rules
    FOR EACH STATEMENT EXECUTE FUNCTION notify_lab_changes();
