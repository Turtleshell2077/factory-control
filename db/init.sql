-- скрипт выполняется один раз, при первом запуске контейнера с пустым томом

CREATE TABLE machines (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    state VARCHAR(20) NOT NULL DEFAULT 'STOPPED',
    temperature NUMERIC(5, 1) NOT NULL DEFAULT 20,
    speed INTEGER NOT NULL DEFAULT 0,
    produced INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE events (
    id SERIAL PRIMARY KEY,
    machine_id INTEGER NOT NULL REFERENCES machines (id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    source VARCHAR(20) NOT NULL,
    message VARCHAR(255) NOT NULL
);

INSERT INTO machines (name, type) VALUES
    ('Токарный станок ЧПУ №1', 'Токарный станок'),
    ('Токарный станок ЧПУ №2', 'Токарный станок'),
    ('Фрезерный станок', 'Фрезерный станок'),
    ('Конвейер сборки', 'Конвейер');
