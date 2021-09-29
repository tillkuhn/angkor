CREATE TABLE IF NOT EXISTS location
(
    id          UUID   DEFAULT uuid_generate_v4(),
    name       TEXT        not null,
    summary     TEXT       ,
    ltype       varchar(32),
    version integer
);

-- INDEXES
CREATE INDEX ON location (ltype);
