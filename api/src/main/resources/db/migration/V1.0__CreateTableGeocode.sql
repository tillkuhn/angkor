-- inspired by https://wikitravel.org/en/Wikitravel:Geographical_hierarchy
CREATE TYPE  geocode_level AS ENUM ('planet','continent', 'continent_section', 'country','region');
CREATE TABLE IF NOT EXISTS geocode
(
    --id SERIAL PRIMARY KEY, default uuid_generate_v4()
    code       VARCHAR PRIMARY KEY,
    parent_code VARCHAR  NOT NULL,
    name       VARCHAR      NOT NULL,
    level      geocode_level not null ,
    --description TEXT,
    coordinates DOUBLE PRECISION[] DEFAULT '{NULL,NULL}',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    --updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

