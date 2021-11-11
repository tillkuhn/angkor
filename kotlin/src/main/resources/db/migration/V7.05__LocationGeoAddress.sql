-- New Column for OSM Geo Data

ALTER TABLE location ADD COLUMN IF NOT EXISTS geo_address TEXT;
