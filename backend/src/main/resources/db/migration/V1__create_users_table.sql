CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL,
    password_hash   VARCHAR(255),
    name            VARCHAR(100) NOT NULL,
    profile_image_url VARCHAR(500),
    experience_level VARCHAR(20) NOT NULL DEFAULT 'BEGINNER',
    daily_goal_minutes INT NOT NULL DEFAULT 60,
    weekly_goal_days   INT NOT NULL DEFAULT 5,
    weekly_goal_minutes INT NOT NULL DEFAULT 300,
    provider        VARCHAR(20) NOT NULL DEFAULT 'LOCAL',
    provider_id     VARCHAR(255),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);

CREATE UNIQUE INDEX idx_users_email ON users (email) WHERE deleted_at IS NULL;
CREATE INDEX idx_users_provider ON users (provider, provider_id) WHERE provider != 'LOCAL';
