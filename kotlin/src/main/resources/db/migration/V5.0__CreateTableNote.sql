-- DDL
CREATE TABLE IF NOT EXISTS note
(
    id            UUID               DEFAULT uuid_generate_v4(),
    notes         TEXT,
    auth_scope   auth_scope default 'PUBLIC',
    created_at    TIMESTAMP          DEFAULT CURRENT_TIMESTAMP,
    created_by    TEXT               DEFAULT 'system'
);

INSERT INTO NOTE (notes) VALUES ('Remember the silk')
