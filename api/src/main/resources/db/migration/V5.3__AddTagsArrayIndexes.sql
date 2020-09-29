--SELECT * FROM dish WHERE 'salat' = ANY (tags);
-- https://stackoverflow.com/questions/51589501/how-to-lowercase-postgresql-array
-- https://stackoverflow.com/questions/39480580/how-to-index-a-string-array-column-for-pg-trgm-term-any-array-column-que
-- https://stackoverflow.com/questions/54270857/jpa-why-named-parameter-in-native-query-is-not-replaced
-- https://stackoverflow.com/questions/14554302/postgres-query-optimization-forcing-an-index-scan
-- SET enable_seqscan = OFF;
-- https://niallburkley.com/blog/index-columns-for-like-in-postgres/
-- The pg_trgm module provides functions and operators for determining the similarity of ASCII alphanumeric text based
-- on trigram matching, as well as index operator classes that support fast searching for similar strings.
CREATE EXTENSION if not exists pg_trgm;
CREATE OR REPLACE FUNCTION text_array(text[]) returns text as $$ select $1::text $$ LANGUAGE sql IMMUTABLE;

DROP INDEX IF EXISTS dish_tags_idx;
DROP INDEX IF EXISTS dish_lower_idx;
CREATE INDEX IF NOT EXISTS  dish_tags_gintonic_idx ON dish USING gin(text_array(tags) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS dish_name_gintonic_idx ON dish USING gin (name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS dish_summary_idx ON dish USING gin (summary gin_trgm_ops);
-- explain analyse SELECT * FROM dish WHERE text_array(tags) ILIKE '%erter%';

DROP INDEX IF EXISTS place_lower_idx;
DROP INDEX IF EXISTS place_lower_idx1;
DROP INDEX IF EXISTS place_tags_idx;
CREATE INDEX IF NOT EXISTS place_tags_gintonic_idx on place using gin(text_array(tags) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS place_area_code_idx ON place (area_code);
CREATE INDEX IF NOT EXISTS place_name_gintonic_idx ON place (name);
CREATE INDEX IF NOT EXISTS place_summary_idx ON note USING gin (summary gin_trgm_ops);

DROP INDEX IF EXISTS note_tags_idx;
CREATE INDEX if not exists note_tags_gintonic_idx ON note using gin(text_array(tags) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS note_summary_idx ON note USING gin (summary gin_trgm_ops);


-- explain analyse SELECT * FROM dish WHERE text_array(tags) ILIKE '%erter%';
