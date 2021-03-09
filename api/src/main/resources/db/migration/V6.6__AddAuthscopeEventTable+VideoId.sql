ALTER TABLE event ADD COLUMN IF NOT EXISTS auth_scope auth_scope default 'PUBLIC';
COMMENT ON COLUMN event.auth_scope IS 'Visibility of row, defaults to PUBLIC';
CREATE INDEX ON event USING btree (auth_scope);

ALTER TABLE place ADD COLUMN IF NOT EXISTS video_id TEXT;
COMMENT ON COLUMN place.video_id IS 'For instance the little hash at the end of the YouTube URL.';

