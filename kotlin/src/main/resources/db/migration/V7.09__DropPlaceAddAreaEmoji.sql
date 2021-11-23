-- Places have transformed into locations
DROP TABLE IF EXISTS place;

-- See https://blog.emojipedia.org/emoji-zwj-sequences-three-letters-many-possibilities/
ALTER TABLE area ADD COLUMN emoji varchar(10) NOT NULL DEFAULT '🌐';
UPDATE area SET emoji = '🇮🇹' WHERE code = 'it';
UPDATE area SET emoji = '🇲🇲' WHERE code = 'mm';

