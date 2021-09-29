CREATE TABLE IF NOT EXISTS location
(
    id          UUID               DEFAULT uuid_generate_v4(),
    ltype       varchar(32),
    name        TEXT not null,
    summary     TEXT,
    primary_url TEXT,
    image_url   TEXT,
    area_code   TEXT,
    coordinates DOUBLE PRECISION[] DEFAULT '{}',
    auth_scope  auth_scope         DEFAULT 'PUBLIC',
    created_at  TIMESTAMP          DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP          DEFAULT CURRENT_TIMESTAMP,
    created_by  UUID               DEFAULT '00000000-0000-0000-0000-000000000001'::uuid,
    updated_by  UUID               DEFAULT '00000000-0000-0000-0000-000000000001'::uuid,
    version     integer
);

-- INDEXES
CREATE INDEX IF NOT EXISTS location_name_idx ON location ((lower(name)));
CREATE INDEX IF NOT EXISTS location_area_code_idx ON location ((lower(area_code)));
CREATE INDEX IF NOT EXISTS location_ltype_idx ON location USING btree (ltype);
CREATE INDEX IF NOT EXISTS  location_auth_scope_idx ON location USING btree (auth_scope);
