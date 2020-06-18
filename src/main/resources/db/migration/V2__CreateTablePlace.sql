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

CREATE TABLE IF NOT EXISTS place
(
    -- https://dba.stackexchange.com/questions/122623/default-value-for-uuid-column-in-postgres
    id          UUID PRIMARY KEY   DEFAULT uuid_generate_v4(),
    -- https://dba.stackexchange.com/questions/20974/should-i-add-an-arbitrary-length-limit-to-varchar-columns
    name        VARCHAR NOT NULL,
    country     VARCHAR(10),
    lotype      VARCHAR(10),
    summary     VARCHAR,
    primary_url VARCHAR,
    image_url   VARCHAR,
    notes       TEXT,
    coordinates DOUBLE PRECISION[] DEFAULT '{NULL,NULL}',
    created_at  TIMESTAMP          DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP          DEFAULT CURRENT_TIMESTAMP,
    created_by  VARCHAR           DEFAULT 'system',
    updated_by  VARCHAR           DEFAULT 'system'
);

