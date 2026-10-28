\encoding UTF8

DROP TABLE IF EXISTS change_log;

CREATE TABLE change_log (
    id           BIGSERIAL    PRIMARY KEY,
    change_type  VARCHAR(10)  NOT NULL
                 CHECK (change_type IN ('INSERT', 'UPDATE', 'DELETE')),
    entity_class VARCHAR(100) NOT NULL,
    entity_id    BIGINT       NOT NULL,
    details      TEXT         NOT NULL,
    changed_at   TIMESTAMP    NOT NULL,
    logged_at    TIMESTAMP    NOT NULL DEFAULT now(),
    message_id   VARCHAR(100) NOT NULL UNIQUE
);

CREATE INDEX idx_change_log_entity ON change_log (entity_class, entity_id);
CREATE INDEX idx_change_log_time   ON change_log (changed_at DESC);
