INSERT INTO app_user (id,login,first_name,last_name,name,emoji)
VALUES ('00000000-0000-0000-0000-000000000009','photoservice','Photo','Service','Photo Service Account','📷');
INSERT INTO app_user (id,login,first_name,last_name,name,emoji)
VALUES ('00000000-0000-0000-0000-000000000010','postservice','Post','Service','Post Service Account','🏤');

-- no longer needed, we can calculate them
ALTER TABLE area DROP COLUMN IF EXISTS emoji;
