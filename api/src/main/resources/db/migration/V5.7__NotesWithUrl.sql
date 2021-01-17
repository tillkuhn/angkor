-- distinguish new and visited places
ALTER TABLE note ADD COLUMN IF NOT EXISTS primary_url TEXT;
