-- New Rating Column

ALTER TABLE location ADD COLUMN IF NOT EXISTS rating smallint DEFAULT 0;
