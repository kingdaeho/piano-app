CREATE TABLE pieces (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT NOT NULL REFERENCES users(id),
    title            VARCHAR(200) NOT NULL,
    composer         VARCHAR(100),
    genre            VARCHAR(50),
    difficulty       INT CHECK (difficulty >= 1 AND difficulty <= 5),
    status           VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED',
    progress_percent INT NOT NULL DEFAULT 0 CHECK (progress_percent >= 0 AND progress_percent <= 100),
    memo             TEXT,
    started_at       TIMESTAMPTZ,
    completed_at     TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at       TIMESTAMPTZ
);

CREATE INDEX idx_pieces_user_id ON pieces (user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_pieces_user_status ON pieces (user_id, status) WHERE deleted_at IS NULL;
