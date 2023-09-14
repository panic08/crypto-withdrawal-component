CREATE TABLE IF NOT EXISTS users_table(
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    account_non_locked BOOL NOT NULL,
    registered_at BIGINT NOT NULL
);
CREATE TABLE IF NOT EXISTS users_data_table(
    id SERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    nickname VARCHAR(255) NOT NULL,
    balance BIGINT NOT NULL,
    role VARCHAR(255) NOT NULL,
    rank VARCHAR(255) NOT NULL,
    server_seed VARCHAR(255) NOT NULL,
    client_seed VARCHAR(255) NOT NULL,

    FOREIGN KEY (user_id) REFERENCES users_table (id)

);
CREATE TABLE IF NOT EXISTS users_activity_table (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    ipaddress VARCHAR(255) NOT NULL,
    browser_name VARCHAR(255) NOT NULL,
    operating_system VARCHAR(255) NOT NULL,
    browser_version VARCHAR(255) NOT NULL,
    timestamp BIGINT NOT NULL,

    FOREIGN KEY (user_id) REFERENCES users_table (id)
);

CREATE TABLE IF NOT EXISTS cryptos_data_table(
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    public_key VARCHAR(255) NOT NULL,
    private_key VARCHAR(255) NOT NULL,

    FOREIGN KEY (user_id) REFERENCES users_table (id)
);