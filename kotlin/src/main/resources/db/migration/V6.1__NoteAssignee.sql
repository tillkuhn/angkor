ALTER TABLE note ADD COLUMN IF NOT EXISTS assignee uuid DEFAULT '00000000-0000-0000-0000-000000000001'::uuid;
UPDATE note SET assignee = created_by;
ALTER TABLE note ADD FOREIGN KEY (assignee) REFERENCES app_user(id) ON DELETE SET DEFAULT;

CREATE INDEX ON note (created_at); -- to support latest on top query


