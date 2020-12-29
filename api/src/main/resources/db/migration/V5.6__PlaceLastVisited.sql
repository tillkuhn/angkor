-- distinguish new and visited places
ALTER TABLE place ADD COLUMN IF NOT EXISTS last_visited date;
