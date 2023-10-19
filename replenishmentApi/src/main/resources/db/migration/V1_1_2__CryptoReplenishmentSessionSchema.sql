CREATE TABLE IF NOT EXISTS crypto_replenishment_sessions_table
(
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    recipient_address VARCHAR(255) NOT NULL,
    amount DOUBLE PRECISION NOT NULL,
    currency VARCHAR(255) NOT NULL,
    until_timestamp BIGINT NOT NULL
);
