-- Should be run against the database/schema used by the app

CREATE TABLE app_refresh_tokens (
    id BIGSERIAL NOT NULL,
    token_id VARCHAR(255) NOT NULL UNIQUE,
    refresh_token TEXT NOT NULL,
    PRIMARY KEY (id)
);
