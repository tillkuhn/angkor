-- DDL
-- inspired by https://wikitravel.org/en/Wikitravel:Geographical_hierarchy
CREATE TYPE  geocode_level AS ENUM ('PLANET','CONTINENT', 'CONT_SECT', 'COUNTRY','REGION');

CREATE TABLE IF NOT EXISTS geocode
(
    code        VARCHAR PRIMARY KEY,
    parent_code VARCHAR,
    name        VARCHAR      NOT NULL,
    level       geocode_level not null ,
    coordinates DOUBLE PRECISION[] DEFAULT '{NULL,NULL}',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX geocode_lower_name_idx ON geocode ((lower(name)));

-- IMPORTS
-- Based on https://wikitravel.org/shared/Category:Continents as we're focused on travel
INSERT INTO geocode (code,parent_code,name,level) values ('earth',null,'The Earth','PLANET');
INSERT INTO geocode (code,parent_code,name,level) values ('eu','earth','Europe','CONTINENT');
INSERT INTO geocode (code,parent_code,name,level) values ('asia','earth','Asia','CONTINENT');
INSERT INTO geocode (code,parent_code,name,level) values ('am-central','earth','Central America','CONTINENT');
INSERT INTO geocode (code,parent_code,name,level) values ('am-north','earth','North America','CONTINENT');
INSERT INTO geocode (code,parent_code,name,level) values ('am-south','earth','South America','CONTINENT');
INSERT INTO geocode (code,parent_code,name,level) values ('middle-east','earth','Middle East','CONTINENT');
INSERT INTO geocode (code,parent_code,name,level) values ('oceania','earth','Oceania','CONTINENT');
INSERT INTO geocode (code,parent_code,name,level) values ('australia','earth','Australia','CONTINENT');
INSERT INTO geocode (code,parent_code,name,level) values ('africa','earth','Africa','CONTINENT');

INSERT INTO geocode (code,parent_code,name,level) values ('asia-se','asia','Southeast asia','CONT_SECT');
INSERT INTO geocode (code,parent_code,name,level) values ('eu-scan','eu','Scandinavia','CONT_SECT');

-- see https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2
INSERT INTO geocode (code,parent_code,name,level) values ('de','eu','Germany','COUNTRY');
INSERT INTO geocode (code,parent_code,name,level) values ('se','eu-sc','Sweden','COUNTRY');
INSERT INTO geocode (code,parent_code,name,level) values ('it','eu','Italy','COUNTRY');
INSERT INTO geocode (code,parent_code,name,level) values ('gr','eu','Greece','COUNTRY');
INSERT INTO geocode (code,parent_code,name,level) values ('th','asia-se','Thailand','COUNTRY');
INSERT INTO geocode (code,parent_code,name,level) values ('my','asia-se','Myanmar','COUNTRY');
INSERT INTO geocode (code,parent_code,name,level) values ('kh','asia-se','Cambodia','COUNTRY'   );
INSERT INTO geocode (code,parent_code,name,level) values ('vn','asia-se','Vietnam','COUNTRY'   );
INSERT INTO geocode (code,parent_code,name,level) values ('ae','middle-east','United Arab Emirates','COUNTRY'   );
INSERT INTO geocode (code,parent_code,name,level) values ('id','asia-se','Indonesie','COUNTRY'   );
INSERT INTO geocode (code,parent_code,name,level) values ('cu','am-central','Cuba','COUNTRY'   );
