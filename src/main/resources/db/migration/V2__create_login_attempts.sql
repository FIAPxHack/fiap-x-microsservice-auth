CREATE TABLE login_attempts
(
    id         UUID PRIMARY KEY,
    email      VARCHAR(255),
    success    BOOLEAN,
    ip_address VARCHAR(100),
    created_at TIMESTAMP NOT NULL
);
