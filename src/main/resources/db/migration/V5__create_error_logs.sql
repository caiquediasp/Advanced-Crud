CREATE TABLE error_logs
(
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_id       UUID       NOT NULL UNIQUE,
    exception_type VARCHAR    NOT NULL,
    message        TEXT,
    request_path   VARCHAR(500),
    stack_trace    TEXT,
    occurred_at    TIMESTAMPTZ NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_error_logs_occurred_at ON error_logs (occurred_at);