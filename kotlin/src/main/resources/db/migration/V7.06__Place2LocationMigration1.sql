-- Add etype to place to ease 1:1 migration of SQL Inserts

ALTER TABLE place ADD COLUMN IF NOT EXISTS  etype varchar(16) DEFAULT 'PLACE';
UPDATE place set etype='PLACE' WHERE etype is null;
