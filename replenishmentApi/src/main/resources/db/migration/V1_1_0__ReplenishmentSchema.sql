CREATE TABLE IF NOT EXISTS replenishments_table(
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    method VARCHAR(255) NOT NULL,
    currency VARCHAR(255) NOT NULL,
    amount DOUBLE PRECISION NOT NULL,
    created_at BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS replenishments_data_table(
    id SERIAL PRIMARY KEY,
    replenishment_id BIGINT UNIQUE NOT NULL,
    ipaddress VARCHAR(255) NOT NULL,
    browser_name VARCHAR(255) NOT NULL,
    operating_system VARCHAR(255) NOT NULL,
    browser_version VARCHAR(255) NOT NULL,

    FOREIGN KEY (replenishment_id) REFERENCES replenishments_table (id)
)