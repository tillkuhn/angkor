-- Add emoji column  to user. B/C of ZWJ Sequences, a single Unicode char is not sufficient
-- See https://blog.emojipedia.org/emoji-zwj-sequences-three-letters-many-possibilities/
ALTER TABLE app_user ADD COLUMN emoji varchar(10) NOT NULL DEFAULT '👤';

-- change user fk constraint, add ON DELETE SET DEFAULT clause
-- apparently drop and recreate is the only option here
ALTER TABLE place DROP CONSTRAINT place_created_by_fkey;
ALTER TABLE place DROP CONSTRAINT place_updated_by_fkey;
ALTER TABLE dish DROP CONSTRAINT dish_created_by_fkey;
ALTER TABLE dish DROP CONSTRAINT dish_updated_by_fkey;
ALTER TABLE note DROP CONSTRAINT note_created_by_fkey;
ALTER TABLE note DROP CONSTRAINT note_updated_by_fkey;

ALTER TABLE place ADD FOREIGN KEY (created_by) REFERENCES app_user(id) ON DELETE SET DEFAULT;
ALTER TABLE place ADD FOREIGN KEY (updated_by) REFERENCES app_user(id) ON DELETE SET DEFAULT;
ALTER TABLE dish ADD FOREIGN KEY (created_by) REFERENCES app_user(id) ON DELETE SET DEFAULT;
ALTER TABLE dish ADD FOREIGN KEY (updated_by) REFERENCES app_user(id) ON DELETE SET DEFAULT;
ALTER TABLE note ADD FOREIGN KEY (created_by) REFERENCES app_user(id) ON DELETE SET DEFAULT;
ALTER TABLE note ADD FOREIGN KEY (updated_by) REFERENCES app_user(id) ON DELETE SET DEFAULT;


