CREATE TABLE goals (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT NOT NULL REFERENCES users(id),
    piece_id     BIGINT REFERENCES pieces(id),
    type         VARCHAR(30) NOT NULL,
    target_value INT NOT NULL,
    target_date  DATE,
    is_active    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_goals_user ON goals (user_id, is_active);
