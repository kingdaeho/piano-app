CREATE TABLE practice_sessions (
    id                      BIGSERIAL PRIMARY KEY,
    user_id                 BIGINT NOT NULL REFERENCES users(id),
    started_at              TIMESTAMPTZ NOT NULL,
    ended_at                TIMESTAMPTZ,
    total_duration_seconds  INT NOT NULL DEFAULT 0,
    memo                    TEXT,
    mood                    VARCHAR(20),
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_practice_sessions_user_started ON practice_sessions (user_id, started_at DESC);

CREATE TABLE practice_session_pieces (
    id               BIGSERIAL PRIMARY KEY,
    session_id       BIGINT NOT NULL REFERENCES practice_sessions(id) ON DELETE CASCADE,
    piece_id         BIGINT NOT NULL REFERENCES pieces(id),
    duration_seconds INT NOT NULL DEFAULT 0,
    order_index      INT NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_psp_session_id ON practice_session_pieces (session_id);
CREATE INDEX idx_psp_piece_id ON practice_session_pieces (piece_id);
