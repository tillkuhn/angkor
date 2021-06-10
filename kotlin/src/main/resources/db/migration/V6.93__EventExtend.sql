-- https://vladmihalcea.com/map-postgresql-hstore-jpa-entity-property-hibernate/
-- How to map a PostgreSQL HStore entity property with JPA and Hibernate
-- Tutorial https://www.postgresqltutorial.com/postgresql-hstore/

ALTER TABLE event ADD COLUMN IF NOT EXISTS action TEXT;
ALTER TABLE event ADD COLUMN IF NOT EXISTS partition INT;
ALTER TABLE event ADD COLUMN IF NOT EXISTS record_offset BIGINT;
ALTER TABLE event ADD COLUMN IF NOT EXISTS topic TEXT;
ALTER TABLE event ADD COLUMN IF NOT EXISTS source TEXT;

ALTER TABLE event RENAME COLUMN created_at TO time;
ALTER TABLE event RENAME COLUMN summary TO message;
ALTER TABLE event RENAME COLUMN created_by TO user_id;

-- name function based index, or it will be named <table>_lower_idx
CREATE INDEX event_action_idx ON event (lower(action));
CREATE INDEX ON event (time);

-- populate default values
UPDATE EVENT set action = concat(lower(event_type::text),':',lower(entity_type::text)) where action is null;
UPDATE EVENT set source = 'angkor-api' where source is null;
UPDATE EVENT set topic = 'app' where topic is null;
UPDATE EVENT set action= 'update:dish'  where action = 'dish_served:dish';
UPDATE EVENT set action=replace(action,'created:','create:');
UPDATE EVENT set action=replace(action,'deleted:','delete:');
UPDATE EVENT set action=replace(action,'updated:','update:');

-- existing indexes
-- CREATE INDEX ON event (entity_id);
-- CREATE INDEX ON event (entity_type);
-- CREATE INDEX ON event (event_type);
-- authscope

-- cleanup stuff no longer needed
DROP INDEX IF EXISTS event_event_type_idx;
DROP INDEX IF EXISTS event_entity_type_idx;
DROP INDEX IF EXISTS event_auth_scope_idx;

ALTER TABLE event drop column if exists auth_scope;
ALTER TABLE event alter column entity_id drop not null;
ALTER TABLE event drop column if exists entity_type;
ALTER TABLE event drop column if exists event_type;
