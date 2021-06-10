-- {
--   "id": "70c4916f-e621-477b-8654-44952491bee1",
--   "coordinates": [
--     13.42714,
--     41.26367
--   ],
--   "area_code": "it",
--   "createdAt": "2019-10-02T18:57:27.534Z",
--   "createdBy": "test@test.de",
--   "imageUrl": "https://www.portanapoli.de/sites/default/files/styles/half_column_250/public/pictures/taxonomy/sperlonga_by_night.jpg?itok=uCh02nl8",
--   "location_type": "BEACH",
--   "name": "Sperlonga",
--   "summary": "Tip Sperlonga mit herrlichen Sandstränden und einer malerischen Altstadt ist einer der schönsten Orte Süditaliens.",
--   "notes": "Sperlonga ist einer der malerischsten Orte Süditaliens.\n Bezaubernd ist der Blick von der Altstadt.",
--   "primaryUrl": "https://www.portanapoli.de/sperlonga",
--   "updatedAt": "2019-11-09T12:15:45.689Z",
--   "updatedBy": "test@test.de"
-- }

-- DDL
CREATE TABLE IF NOT EXISTS place
(
    -- https://dba.stackexchange.com/questions/122623/default-value-for-uuid-column-in-postgres
    id            UUID               DEFAULT uuid_generate_v4(),
    -- https://dba.stackexchange.com/questions/20974/should-i-add-an-arbitrary-length-limit-to-varchar-columns
    name          TEXT NOT NULL,
    summary       TEXT,
    notes         TEXT,
    area_code     TEXT, -- FK on geocode COUNTRY
    primary_url   VARCHAR,
    image_url     VARCHAR,
    tags          TEXT[]             DEFAULT '{}',
    auth_scope    auth_scope         default 'PUBLIC',
    created_at    TIMESTAMP          DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP          DEFAULT CURRENT_TIMESTAMP,
    created_by    TEXT               DEFAULT 'system',
    updated_by    TEXT               DEFAULT 'system',
    -- place specific
    location_type location_type      default 'PLACE',
    coordinates   DOUBLE PRECISION[] DEFAULT '{}',

    PRIMARY KEY (id),
    FOREIGN KEY (area_code) REFERENCES area (code)
);

-- To create an index on the expression lower(area_code), allowing efficient case-insensitive searches:
--  we have chosen to omit the index name, so the system will choose a name, typically films_lower_idx.
CREATE INDEX ON place ((lower(name)));
CREATE INDEX ON place ((lower(area_code)));
CREATE INDEX ON place USING btree (auth_scope);
CREATE INDEX ON place USING gin (tags);

-- IMPORTS
-- @13.7244416,100.3529157 (Lat, Lon) geojson = [100.523186, 13.736717]  **(Lon,Lat)!!!**
INSERT INTO PLACE (name, area_code, summary, coordinates, image_url, location_type)
VALUES ('LaPaDu Duisburg', 'de', 'Good place for photos', '{6.7788039,51.4817111}',
        'https://media04.lokalkompass.de/article/2015/12/29/1/7778421_L.jpg?1561933573', 'EXCURS');
INSERT INTO PLACE (name, area_code, summary, coordinates, image_url, location_type)
VALUES ('Essen Downtown', 'de', 'Nicer as expected', '{7.9371, 52.72258}',
        'https://www.guenter-pilger.de/media_u6/ruhr/Ruhr_bei_Horst_$2.jpg', 'CITY');
INSERT INTO PLACE (name, area_code, summary, coordinates, image_url, location_type)
VALUES ('Bangkok Lumphini Park', 'th', 'Just one night', '{100.5420317,13.7287306}',
        'https://upload.wikimedia.org/wikipedia/commons/thumb/3/3e/Aerial_view_of_Lumphini_Park.jpg/640px-Aerial_view_of_Lumphini_Park.jpg',
        'EXCURS');
INSERT INTO PLACE (name, area_code, summary, coordinates, image_url, location_type)
VALUES ('Palermo City', 'it', 'Pizza Pizza', '{13.366667,38.116669}',
        'https://upload.wikimedia.org/wikipedia/commons/thumb/e/e0/Panoramica_Cattedrale_di_Palermo.jpg/550px-Panoramica_Cattedrale_di_Palermo.jpg',
        'CITY');
INSERT INTO PLACE (name, area_code, summary, coordinates, image_url, location_type)
VALUES ('Casa los Mangos', 'cu', 'Best place in Trinidad', '{-79.9947085,21.7987285}',
        'https://timafe.files.wordpress.com/2015/12/img_2214_schirm_sunset_mini.jpg', 'ACCOM');
INSERT INTO PLACE (name, area_code, summary, coordinates, image_url, location_type)
VALUES ('Angkor Thom', 'kh', 'Temple must see', '{103.8567631,13.4411937}',
        'https://timafe.files.wordpress.com/2016/05/img_3798_3faces.jpg?w=480', 'MONUM');
INSERT INTO PLACE (name, area_code, summary, coordinates, image_url, location_type, auth_scope)
VALUES ('Hobbittal bei den 6 Seen', 'de', 'TiMaFe Secret Place', '{6.8234614,51.3681216}',
        'https://encrypted-tbn0.gstatic.com/images?q=tbn%3AANd9GcTr4oUaC5iv0h-XDy8PeW2LJCkbDvQ2E64stA&usqp=CAU',
        'EXCURS', 'ALL_AUTH');

