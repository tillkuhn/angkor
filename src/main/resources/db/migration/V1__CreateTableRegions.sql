CREATE TABLE IF NOT EXISTS region
(
    --id SERIAL PRIMARY KEY,
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code       VARCHAR(10)  NOT NULL,
    parentCode VARCHAR(10)  NOT NULL,
    name       VARCHAR      NOT NULL,
    --description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    --updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- insert into region (code,parentCode,name) values ('de','eu','Germany')
