ALTER TABLE link
    ADD COLUMN IF NOT EXISTS coordinates DOUBLE PRECISION[] DEFAULT '{}';

UPDATE LINK set coordinates = '{103.2151612,13.06907}' WHERE name like '%Bamboo Train%' ;
UPDATE LINK set coordinates = '{102.0964487,19.8205794}' where name like '%Nahm Dong Park%' ;
UPDATE LINK set coordinates = '{-83.7625568,22.6061363}' where name like '%El Fortin%';
