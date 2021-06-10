CREATE TYPE entity_type AS ENUM ( 'DISH','PLACE','NOTE','AREA','USER');
CREATE TYPE event_type AS ENUM ( 'CREATED','UPDATED','DELETED','DISH_SERVED','PLACE_VISITED');

-- DDL for table
CREATE TABLE IF NOT EXISTS event
(
    id          UUID      DEFAULT uuid_generate_v4(),
    entity_id   UUID      not null,
    entity_type entity_type not null,
    event_type  event_type  not null,
    summary     TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by  UUID      DEFAULT '00000000-0000-0000-0000-000000000001'::uuid
);

ALTER TABLE event
    ADD FOREIGN KEY (created_by) REFERENCES app_user (id) ON DELETE SET DEFAULT;

CREATE INDEX ON event (entity_id);
CREATE INDEX ON event (entity_type);
CREATE INDEX ON event (event_type);
