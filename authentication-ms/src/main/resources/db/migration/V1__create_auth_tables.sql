-- 1. Create the users table
CREATE TABLE users
(
    id            UUID PRIMARY KEY,
    name          VARCHAR(255) NOT NULL UNIQUE,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(50)  NOT NULL DEFAULT 'PATIENT'
);

-- 2. Create the refresh_tokens table
CREATE TABLE refresh_tokens
(
    id          UUID PRIMARY KEY,
    user_id     UUID                     NOT NULL,
    token       VARCHAR(255)             NOT NULL UNIQUE,
    expiry_date TIMESTAMP WITH TIME ZONE NOT NULL,

    -- Foreign key to link refresh_tokens to users
    CONSTRAINT fk_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);

-- 3. Create an index to optimize searching for tokens by user
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);