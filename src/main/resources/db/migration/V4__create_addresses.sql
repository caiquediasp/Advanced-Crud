CREATE TABLE addresses
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    public_id    UUID         NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    user_id      BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    zip_code     VARCHAR(8)   NOT NULL,
    street       VARCHAR(255) NOT NULL,
    number       VARCHAR(20),
    complement   VARCHAR(100),
    neighborhood VARCHAR(100) NOT NULL,
    city         VARCHAR(100) NOT NULL,
    state        CHAR(2)      NOT NULL,
    is_primary   BOOLEAN      NOT NULL        DEFAULT FALSE,
    created_at   TIMESTAMPTZ  NOT NULL        DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL        DEFAULT now()
);

CREATE INDEX idx_addresses_user_id ON addresses (user_id);