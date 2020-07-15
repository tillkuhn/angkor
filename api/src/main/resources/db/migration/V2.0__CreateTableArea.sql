-- DDL
-- inspired by https://wikitravel.org/en/Wikitravel:Geographical_hierarchy
CREATE TABLE IF NOT EXISTS area
(
    code        TEXT PRIMARY KEY,
    parent_code TEXT,
    name        TEXT      NOT NULL,
    level       area_level NOT NULL ,
    coordinates DOUBLE PRECISION[] DEFAULT '{}',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX area_lower_name_idx ON area ((lower(name)));

-- IMPORTS
-- CONTINENTS + SUB, Based on https://wikitravel.org/shared/Category:Continents as we're focused on travel

INSERT INTO area (code,parent_code,name,level) values ('africa','earth','Africa','CONTINENT');
INSERT INTO area (code,parent_code,name,level) values ('am-central','earth','Central America','CONTINENT');
INSERT INTO area (code,parent_code,name,level) values ('am-north','earth','North America','CONTINENT');
INSERT INTO area (code,parent_code,name,level) values ('am-south','earth','South America','CONTINENT');
INSERT INTO area (code,parent_code,name,level) values ('asia','earth','Asia','CONTINENT');
INSERT INTO area (code,parent_code,name,level) values ('asia-se','asia','Southeast asia','CONT_SECT');
INSERT INTO area (code,parent_code,name,level) values ('australia','earth','Australia','CONTINENT');
INSERT INTO area (code,parent_code,name,level) values ('earth',null,'The Earth','PLANET');
INSERT INTO area (code,parent_code,name,level) values ('eu-scan','europe','Scandinavia','CONT_SECT');
INSERT INTO area (code,parent_code,name,level) values ('europe','earth','Europe','CONTINENT');
INSERT INTO area (code,parent_code,name,level) values ('middle-east','earth','Middle East','CONTINENT');
INSERT INTO area (code,parent_code,name,level) values ('oceania','earth','Oceania','CONTINENT');

-- COUNTRIES; see https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2
INSERT INTO area (code,parent_code,name,level) values ('ae','middle-east','United Arab Emirates','COUNTRY'   );
INSERT INTO area (code,parent_code,name,level) values ('al','europe','Albania','COUNTRY');
INSERT INTO area (code,parent_code,name,level) values ('at','europe','Austria','COUNTRY');
INSERT INTO area (code,parent_code,name,level) values ('cn','asia','China','COUNTRY'   );
INSERT INTO area (code,parent_code,name,level) values ('cr','am-central','Costa Rica','COUNTRY'   );
INSERT INTO area (code,parent_code,name,level) values ('cu','am-central','Cuba','COUNTRY'   );
INSERT INTO area (code,parent_code,name,level) values ('cz','europe','Czech','COUNTRY');
INSERT INTO area (code,parent_code,name,level) values ('de','europe','Germany','COUNTRY');
INSERT INTO area (code,parent_code,name,level) values ('es','europe','Spain','COUNTRY');
INSERT INTO area (code,parent_code,name,level) values ('gr','europe','Greece','COUNTRY');
INSERT INTO area (code,parent_code,name,level) values ('hr','europe','Croatia','COUNTRY');
INSERT INTO area (code,parent_code,name,level) values ('id','asia-se','Indonesie','COUNTRY'   );
INSERT INTO area (code,parent_code,name,level) values ('in','asia','India','COUNTRY'   );
INSERT INTO area (code,parent_code,name,level) values ('it','europe','Italy','COUNTRY');
INSERT INTO area (code,parent_code,name,level) values ('kh','asia-se','Cambodia','COUNTRY'   );
INSERT INTO area (code,parent_code,name,level) values ('la','asia-se','Laos','COUNTRY'   );
INSERT INTO area (code,parent_code,name,level) values ('lk','asia','Sri Lanka','COUNTRY'   );
INSERT INTO area (code,parent_code,name,level) values ('ma','africa','Morocco','COUNTRY'   );
INSERT INTO area (code,parent_code,name,level) values ('me','europe','Monte Negro','COUNTRY');
INSERT INTO area (code,parent_code,name,level) values ('mm','asia-se','Myanmar','COUNTRY');
INSERT INTO area (code,parent_code,name,level) values ('my','asia-se','Malaysia','COUNTRY'   );
INSERT INTO area (code,parent_code,name,level) values ('nl','europe','Netherlands','COUNTRY');
INSERT INTO area (code,parent_code,name,level) values ('pt','europe','Portugal','COUNTRY');
INSERT INTO area (code,parent_code,name,level) values ('sc','europe','Seychellen','COUNTRY'   );
INSERT INTO area (code,parent_code,name,level) values ('se','eu-sc','Sweden','COUNTRY');
INSERT INTO area (code,parent_code,name,level) values ('sg','asia-se','Singapore','COUNTRY'   );
INSERT INTO area (code,parent_code,name,level) values ('st','europe','São Tomé & Príncipe','COUNTRY'   );
INSERT INTO area (code,parent_code,name,level) values ('th','asia-se','Thailand','COUNTRY');
INSERT INTO area (code,parent_code,name,level) values ('tr','europe','Turkey','COUNTRY');
INSERT INTO area (code,parent_code,name,level) values ('vn','asia-se','Vietnam','COUNTRY'   );

-- REGIONS within a country
INSERT INTO area (code,parent_code,name,level) values ('th-north','th','North Thailand','REGION');
INSERT INTO area (code,parent_code,name,level) values ('th-gulf','th','Thailand Gulf','REGION');
