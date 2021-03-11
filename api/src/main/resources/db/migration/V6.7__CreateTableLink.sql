CREATE TYPE media_type AS ENUM ( 'VIDEO','AUDIO','IMAGE','PDF','DEFAULT');
CREATE TABLE IF NOT EXISTS link
(
    id          UUID       DEFAULT uuid_generate_v4(),
    link_url    TEXT NOT NULL,
    name        TEXT,
    media_type  media_type DEFAULT 'DEFAULT'::media_type,
    created_at  TIMESTAMP  DEFAULT CURRENT_TIMESTAMP,
    created_by  UUID       DEFAULT '00000000-0000-0000-0000-000000000001'::uuid,
    auth_scope  auth_scope default 'PUBLIC'::auth_scope,
    entity_type entity_type DEFAULT null,
    entity_id   UUID DEFAULT null
);

ALTER TABLE event
    ADD FOREIGN KEY (created_by) REFERENCES app_user (id) ON DELETE SET DEFAULT;

-- indexes
CREATE INDEX ON link (entity_id);
CREATE INDEX ON link ((lower(name)));
CREATE INDEX ON link USING btree (media_type);
CREATE INDEX ON link USING btree (entity_type);
CREATE INDEX ON link USING btree (auth_scope);

-- no longer necessary
ALTER TABLE place DROP COLUMN IF EXISTS video_id;
