/*
 * oauth2-utils
 * Copyright (C) 2020 Craig Miller
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

-- Should be run against the database/schema used by the app

CREATE SEQUENCE app_refresh_tokens_id_seq START 1;

CREATE TABLE app_refresh_tokens (
    id BIGINT NOT NULL DEFAULT nextval('app_refresh_tokens_id_seq'::regclass),
    token_id VARCHAR(255) NOT NULL UNIQUE,
    refresh_token TEXT NOT NULL,
    CONSTRAINT app_refresh_tokens_id_pk PRIMARY KEY (id)
);
