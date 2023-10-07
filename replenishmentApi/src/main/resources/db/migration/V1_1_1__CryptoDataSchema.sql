CREATE TABLE IF NOT EXISTS crypto_data_table
(
    id SERIAL PRIMARY KEY,
    address VARCHAR(255) NOT NULL,
    private_key VARCHAR(255) NOT NULL,
    currency VARCHAR(255) NOT NULL
);