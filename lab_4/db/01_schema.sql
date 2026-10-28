\encoding UTF8

DROP TABLE IF EXISTS chunks;
DROP TABLE IF EXISTS documents;

CREATE TABLE documents (
    id          BIGSERIAL    PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    source      VARCHAR(500),
    language    VARCHAR(10)  NOT NULL DEFAULT 'ru',
    content     TEXT         NOT NULL,
    token_count INTEGER      NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE chunks (
    id          BIGSERIAL PRIMARY KEY,
    document_id BIGINT    NOT NULL REFERENCES documents (id) ON DELETE CASCADE,
    chunk_index INTEGER   NOT NULL,
    content     TEXT      NOT NULL,
    token_count INTEGER   NOT NULL DEFAULT 0
);

CREATE INDEX idx_chunks_document ON chunks (document_id, chunk_index);
