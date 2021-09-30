-- Table DDL
CREATE TABLE IF NOT EXISTS location
(
    id          UUID               DEFAULT uuid_generate_v4(),
    etype       varchar(16),
    external_id TEXT,
    name        TEXT not null,
    summary     TEXT,
    notes       TEXT,
    primary_url TEXT,
    image_url   TEXT,
    area_code   TEXT,
    coordinates DOUBLE PRECISION[] DEFAULT '{}',
    tags        TEXT[]             DEFAULT '{}',
    auth_scope  auth_scope         DEFAULT 'PUBLIC',
    created_at  TIMESTAMP          DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP          DEFAULT CURRENT_TIMESTAMP,
    created_by  UUID               DEFAULT '00000000-0000-0000-0000-000000000001'::uuid,
    updated_by  UUID               DEFAULT '00000000-0000-0000-0000-000000000001'::uuid,
    version     INTEGER,

    PRIMARY KEY (id),
    FOREIGN KEY (area_code) REFERENCES area (code),
    FOREIGN KEY (created_by) REFERENCES app_user(id),
    FOREIGN KEY (updated_by) REFERENCES app_user(id)
);

-- Indexes
CREATE INDEX IF NOT EXISTS location_name_idx ON location ((lower(name)));
CREATE INDEX IF NOT EXISTS location_area_code_idx ON location ((lower(area_code)));
CREATE INDEX IF NOT EXISTS location_etype_idx ON location USING btree (etype);
CREATE INDEX IF NOT EXISTS location_auth_scope_idx ON location USING btree (auth_scope);
CREATE INDEX IF NOT EXISTS location_tags_idx ON location USING gin (tags);
CREATE UNIQUE INDEX IF NOT EXISTS location_external_id ON location (etype, external_id);

-- Worth commenting
COMMENT ON TABLE  location IS 'Main table for anything that can be put on a map';
COMMENT ON COLUMN location.etype IS 'Entity type, used as discriminator column';
COMMENT ON COLUMN location.coordinates IS 'Order is [Lng,Lat] (Longitude, Latitude)';
COMMENT ON COLUMN location.version IS 'JPA uses a version field in entities to detect concurrent modifications';
