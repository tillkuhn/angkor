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
CREATE TABLE IF NOT EXISTS dish
(
    -- https://dba.stackexchange.com/questions/122623/default-value-for-uuid-column-in-postgres
    id          UUID    DEFAULT uuid_generate_v4(),
    -- https://dba.stackexchange.com/questions/20974/should-i-add-an-arbitrary-length-limit-to-varchar-columns
    name        VARCHAR NOT NULL,
    authenticName        VARCHAR NOT NULL,
    summary     VARCHAR,
    notes       TEXT,
    country     VARCHAR, -- FK on geocode COUNTRY
    primary_url VARCHAR,
    image_url   VARCHAR,
    created_at  TIMESTAMP          DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP          DEFAULT CURRENT_TIMESTAMP,
    created_by  VARCHAR            DEFAULT 'system',
    updated_by  VARCHAR            DEFAULT 'system',
    PRIMARY KEY (id),
    FOREIGN KEY (country) REFERENCES geocode(code)
);

INSERT INTO dish (name,authenticName,country,primary_url) VALUES ('Greek Salad','ελληνική σαλάτα','gr','http://de.allrecipes.com/rezept/1268/authentischer-griechischer-salat.aspx');
