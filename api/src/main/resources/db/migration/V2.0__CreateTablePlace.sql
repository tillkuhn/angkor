-- {
--   "id": "70c4916f-e621-477b-8654-44952491bee1",
--   "coordinates": [
--     13.42714,
--     41.26367
--   ],
--   "country": "it",
--   "createdAt": "2019-10-02T18:57:27.534Z",
--   "createdBy": "test@test.de",
--   "imageUrl": "https://www.portanapoli.de/sites/default/files/styles/half_column_250/public/pictures/taxonomy/sperlonga_by_night.jpg?itok=uCh02nl8",
--   "lotype": "BEACH",
--   "name": "Sperlonga",
--   "notes": "Sperlonga ist einer der malerischsten Orte Süditaliens.\n Bezaubernd ist der Blick von der Altstadt.",
--   "primaryUrl": "https://www.portanapoli.de/sperlonga",
--   "summary": "Tip Sperlonga mit herrlichen Sandstränden und einer malerischen Altstadt ist einer der schönsten Orte Süditaliens.",
--   "updatedAt": "2019-11-09T12:15:45.689Z",
--   "updatedBy": "test@test.de"
-- }

CREATE TYPE location_type AS ENUM(  'PLACE','ACCOM','BEACH','CITY','EXCURS','MONUM','MOUNT','ROAD');

CREATE TABLE IF NOT EXISTS place
(
    -- https://dba.stackexchange.com/questions/122623/default-value-for-uuid-column-in-postgres
    id          UUID    DEFAULT uuid_generate_v4(),
    -- https://dba.stackexchange.com/questions/20974/should-i-add-an-arbitrary-length-limit-to-varchar-columns
    name        VARCHAR NOT NULL,
    country     VARCHAR REFERENCES geocode(code),
    lotype      location_type default 'BEACH',
    summary     VARCHAR,
    primary_url VARCHAR,
    image_url   VARCHAR,
    notes       TEXT,
    coordinates DOUBLE PRECISION[] DEFAULT '{NULL,NULL}',
    created_at  TIMESTAMP          DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP          DEFAULT CURRENT_TIMESTAMP,
    created_by  VARCHAR           DEFAULT 'system',
    updated_by  VARCHAR           DEFAULT 'system',
    PRIMARY KEY (id),
    FOREIGN KEY (country) REFERENCES geocode(code)
);

-- To create an index on the expression lower(country), allowing efficient case-insensitive searches:
--  we have chosen to omit the index name, so the system will choose a name, typically films_lower_idx.
CREATE INDEX ON place ((lower(country)));

-- Bangkok Latitude and longitude coordinates are:
-- @13.7244416,100.3529157 (Lat, Lon) geojson = [100.523186, 13.736717]  **(Lon,Lat)!!!**

INSERT INTO PLACE (name,country,summary,coordinates,image_url,lotype) VALUES ('LaPaDu Duisburg','de','Good place for photos','{6.7788039,51.4817111}','https://media04.lokalkompass.de/article/2015/12/29/1/7778421_L.jpg?1561933573','EXCURS');
INSERT INTO PLACE (name,country,summary,coordinates,image_url) VALUES ('Essen Downtown','de','Nicer as expected','{7.9371, 52.72258}','https://www.guenter-pilger.de/media_u6/ruhr/Ruhr_bei_Horst_$2.jpg');
INSERT INTO PLACE (name,country,summary,coordinates,image_url) VALUES ('Bangkok Lumphini Park','th','Just one night','{100.5420317,13.7287306}','https://upload.wikimedia.org/wikipedia/commons/thumb/3/3e/Aerial_view_of_Lumphini_Park.jpg/640px-Aerial_view_of_Lumphini_Park.jpg');
INSERT INTO PLACE (name,country,summary,coordinates,image_url,lotype) VALUES ('Palermo','it','Pizza Pizza','{13.366667,38.116669}','https://upload.wikimedia.org/wikipedia/commons/thumb/e/e0/Panoramica_Cattedrale_di_Palermo.jpg/550px-Panoramica_Cattedrale_di_Palermo.jpg','CITY');
INSERT INTO PLACE (name,country,summary,coordinates,image_url,lotype) VALUES ('Casa los Mangos','cu','Best place in Trinidad','{-79.9947085,21.7987285}','https://timafe.files.wordpress.com/2015/12/img_2214_schirm_sunset_mini.jpg','ACCOM');

