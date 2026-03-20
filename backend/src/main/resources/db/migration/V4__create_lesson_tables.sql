CREATE TABLE lesson_notes (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT NOT NULL REFERENCES users(id),
    lesson_number    INT NOT NULL,
    lesson_date      DATE NOT NULL,
    start_time       TIME,
    end_time         TIME,
    content          TEXT,
    teacher_feedback TEXT,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at       TIMESTAMPTZ
);

CREATE INDEX idx_lesson_notes_user ON lesson_notes (user_id, lesson_date DESC) WHERE deleted_at IS NULL;

CREATE TABLE lesson_note_pieces (
    id             BIGSERIAL PRIMARY KEY,
    lesson_note_id BIGINT NOT NULL REFERENCES lesson_notes(id) ON DELETE CASCADE,
    piece_id       BIGINT NOT NULL REFERENCES pieces(id),
    UNIQUE (lesson_note_id, piece_id)
);

CREATE TABLE lesson_assignments (
    id             BIGSERIAL PRIMARY KEY,
    lesson_note_id BIGINT NOT NULL REFERENCES lesson_notes(id) ON DELETE CASCADE,
    content        VARCHAR(500) NOT NULL,
    is_completed   BOOLEAN NOT NULL DEFAULT FALSE,
    order_index    INT NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_lesson_assignments_note ON lesson_assignments (lesson_note_id);
