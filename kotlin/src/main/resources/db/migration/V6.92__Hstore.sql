-- https://vladmihalcea.com/map-postgresql-hstore-jpa-entity-property-hibernate/
-- How to map a PostgreSQL HStore entity property with JPA and Hibernate
-- Tutorial https://www.postgresqltutorial.com/postgresql-hstore/
CREATE EXTENSION IF NOT EXISTS hstore;

ALTER TABLE link ADD COLUMN IF NOT EXISTS properties hstore;
