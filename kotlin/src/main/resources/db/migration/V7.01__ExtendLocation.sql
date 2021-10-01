-- Table DDL
ALTER TABLE location ADD COLUMN IF NOT EXISTS properties hstore;
ALTER TABLE location ADD COLUMN IF NOT EXISTS been_there date;
ALTER TABLE location ADD COLUMN IF NOT EXISTS ltype location_type default 'PLACE';

-- Indexes and constraints

-- Worth commenting
COMMENT ON COLUMN location.properties IS 'hstore allows storing sets of key/value pairs within a single PostgreSQL value';
