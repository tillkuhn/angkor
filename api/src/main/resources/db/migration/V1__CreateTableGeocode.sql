CREATE TABLE IF NOT EXISTS geocode
(
    --id SERIAL PRIMARY KEY, default uuid_generate_v4()
    code       VARCHAR(10)  NOT NULL,
    parentCode VARCHAR(10)  NOT NULL,
    name       VARCHAR      NOT NULL,
    --description TEXT,
    coordinates DOUBLE PRECISION[] DEFAULT '{NULL,NULL}',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    --updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

