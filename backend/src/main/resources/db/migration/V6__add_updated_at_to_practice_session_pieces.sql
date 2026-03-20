ALTER TABLE practice_session_pieces
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW();
