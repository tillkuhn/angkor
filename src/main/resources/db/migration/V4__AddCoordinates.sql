--ALTER TABLE place ADD COLUMN IF NOT EXISTS coordinates public.geometry;
ALTER TABLE place ADD COLUMN IF NOT EXISTS coordinates double precision[];
