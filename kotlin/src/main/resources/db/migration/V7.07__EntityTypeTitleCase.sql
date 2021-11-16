-- Refactor enums so they consistently represent titlecase values across all layers
-- also add new values introduced with location table
-- Rename Enums: https://stackoverflow.com/a/45444822/4292075

ALTER TYPE entity_type RENAME VALUE 'DISH' TO 'Dish';
ALTER TYPE entity_type RENAME VALUE 'PLACE' TO 'Place';
ALTER TYPE entity_type RENAME VALUE 'NOTE' TO 'Note';
ALTER TYPE entity_type RENAME VALUE 'AREA' TO 'Area';
ALTER TYPE entity_type RENAME VALUE 'USER' TO 'User';
ALTER TYPE entity_type ADD VALUE IF NOT EXISTS 'Tour';
ALTER TYPE entity_type ADD VALUE IF NOT EXISTS 'Video';
ALTER TYPE entity_type ADD VALUE IF NOT EXISTS 'Post';
ALTER TYPE entity_type ADD VALUE IF NOT EXISTS 'Photo';

