-- Guten Tag!
-- DDL
CREATE TABLE IF NOT EXISTS tag
(
    id          UUID   DEFAULT gen_random_uuid(),
    label       TEXT        not null,
    entity_type entity_type not null,
    keywords    TEXT[] DEFAULT '{}',
    UNIQUE (label, entity_type)
);

-- INDEXES
CREATE INDEX ON tag (label);
CREATE INDEX ON tag (entity_type);

-- INITIAL IMPORTS
INSERT INTO tag (label,entity_type,keywords)
    VALUES ('watch','NOTE','{"youtube", "mediathek"}');
INSERT INTO tag (label,entity_type,keywords)
    VALUES ('spicy','DISH','{"chili", "scharf"}');
INSERT INTO tag (label,entity_type,keywords)
    VALUES ('island','PLACE','{"insel"}');
