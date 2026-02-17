CREATE TABLE refresh_tokens
(
    id         UUID PRIMARY KEY,
    user_id    UUID         NOT NULL,
    token      VARCHAR(500) NOT NULL,
    expires_at TIMESTAMP    NOT NULL,
    revoked    BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL
);