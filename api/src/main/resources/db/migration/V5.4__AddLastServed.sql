UPDATE dish set times_served = 0 where times_served is null;
UPDATE dish set rating = 0 where rating is null;

ALTER TABLE dish ALTER COLUMN times_served SET NOT NULL;
ALTER TABLE dish ALTER COLUMN rating SET NOT NULL;

ALTER TABLE dish ALTER COLUMN times_served SET DEFAULT 0;
ALTER TABLE dish ALTER COLUMN rating SET DEFAULT 0;

ALTER TABLE dish ADD COLUMN last_served TIMESTAMP;
