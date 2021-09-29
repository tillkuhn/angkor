CREATE TABLE IF NOT EXISTS location
(
    id          UUID               DEFAULT uuid_generate_v4(),
    ltype       varchar(32),
    name        TEXT not null,
    summary     TEXT,
    coordinates DOUBLE PRECISION[] DEFAULT '{}',
    created_at  TIMESTAMP          DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP          DEFAULT CURRENT_TIMESTAMP,
    created_by  UUID               DEFAULT '00000000-0000-0000-0000-000000000001'::uuid,
    updated_by  UUID               DEFAULT '00000000-0000-0000-0000-000000000001'::uuid,
    version     integer
);

-- INDEXES
CREATE INDEX ON location ((lower(name)));
CREATE INDEX ON location USING btree (ltype);
