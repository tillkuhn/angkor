-- Add emoji column  to user. B/C of ZWJ Sequences, a single Unicode char is not sufficient
-- See https://blog.emojipedia.org/emoji-zwj-sequences-three-letters-many-possibilities/
--ALTER TABLE app_user ADD COLUMN emoji varchar(10) NOT NULL DEFAULT '👤';
INSERT INTO  app_user
    (id,login,first_name,last_name,name,email,activated,roles,emoji)
    values
    ('00000000-0000-0000-0000-000000000002'::uuid,'hase','Hase','Klaus','Hase Name','hase@klaus.de',false,'{ROLE_USER}','👤')
ON CONFLICT DO NOTHING;

UPDATE app_user set name='system' where id='00000000-0000-0000-0000-000000000001'::uuid;
