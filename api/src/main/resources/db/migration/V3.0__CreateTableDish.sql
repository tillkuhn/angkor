-- {
--         "origin": "gr",
--         "lastServed": "2015-07-07T21:33:37.718Z",
--         "authenticName": "ελληνική σαλάτα",
--         "timesServed": 5,
--         "rating": 9,
--         "createdAt": "2015-08-09-09T22:22:22.222Z",
--         "primaryUrl": "http://de.allrecipes.com/rezept/1268/authentischer-griechischer-salat.aspx",
--         "updatedBy": "kuhnibaert@gmail.com",
--         "name": "Griechischer Salat",
--         "imageUrl": "http://www.casualcatering.com/shop/images/greek.jpg",
--         "updatedAt": "2018-09-19T21:58:30.910Z",
--         "notes": "1. Paprikaschoten halbieren, entkernen und in 2 cm große Würfel schneiden. Tomaten sechsteln. Salatgurke längs vierteln und quer in 2 cm große Stücke schneiden. Zwiebeln in 1 cm dicke Scheiben schneiden. Minze in feine Streifen schneiden. Oliven halbieren, Schafskäse in 2 cm große Würfel schneiden.\n\n2. Essig mit 10 El kaltem Wasser, Öl, Salz und Pfeffer in einer Schüssel verrühren. Paprikaschoten, Tomaten, Gurke, Zwiebeln, Minze, Schafskäse und Oliven mit dem Dressing mischen und kurz durchziehen lassen. Dazu passt Fladenbrot.",
--         "id": "5585e234e4b062eca3674e08",
--         "tags": [
--             "feta",
--             "oliven",
--             "salat",
--             "tomaten",
--             "veggy"
--         ]
--     }

-- DDL for table
CREATE TABLE IF NOT EXISTS dish
(
    -- https://dba.stackexchange.com/questions/122623/default-value-for-uuid-column-in-postgres
    id           UUID       DEFAULT uuid_generate_v4(),
    -- https://dba.stackexchange.com/questions/20974/should-i-add-an-arbitrary-length-limit-to-varchar-columns
    name         TEXT NOT NULL,
    summary      TEXT,
    notes        TEXT,
    area_code    TEXT, -- FK on geocode COUNTRY, formerly known as origin
    primary_url  TEXT,
    image_url    TEXT,
    tags         TEXT[]     DEFAULT '{}',
    auth_scope   auth_scope default 'PUBLIC',
    created_at   TIMESTAMP  DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP  DEFAULT CURRENT_TIMESTAMP,
    created_by   TEXT       DEFAULT 'system',
    updated_by   TEXT       DEFAULT 'system',
    -- // dish specific
    times_served smallint,
    rating       smallint,

    PRIMARY KEY (id),
    FOREIGN KEY (area_code) REFERENCES area (code)
);

-- INDEXES
CREATE INDEX ON dish ((lower(name)));
CREATE INDEX ON dish USING btree (auth_scope);
CREATE INDEX ON dish USING gin (tags);

-- IMPORTS
INSERT INTO dish (name, summary, area_code, primary_url)
VALUES ('Greek Salad', 'ελληνική σαλάτα', 'gr',
        'http://de.allrecipes.com/rezept/1268/authentischer-griechischer-salat.aspx');
INSERT INTO dish (name, summary, area_code, primary_url)
VALUES ('Tom Yum Suppe', 'ต้มยำกุ้ง', 'th', 'http://www.eatingthaifood.com/2014/08/tom-yum-soup-recipe/');
