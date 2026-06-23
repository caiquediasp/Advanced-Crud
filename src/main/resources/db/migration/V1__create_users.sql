CREATE TABLE users
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    public_id     UUID         NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    name          VARCHAR(150) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(72)  NOT NULL,
    enabled       BOOLEAN      NOT NULL        DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL        DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL        DEFAULT now(),
    deleted_at    TIMESTAMPTZ
);

CREATE UNIQUE INDEX uq_users_email_active
    ON users (email)
    WHERE deleted_at IS NULL;