-- var id: String? = null,
-- var login: String? = null,
-- var firstName: String? = null,
-- var lastName: String? = null,
-- var name: String? = null,
-- var email: String? = null,
-- var imageUrl: String? = null,
-- var activated: Boolean = false,
-- var roles: List<String> = listOf()
-- var createdAt: LocalDateTime? = LocalDateTime.now(),
-- var updatedAt: LocalDateTime? = LocalDateTime.now(),
CREATE TABLE IF NOT EXISTS app_user
(
    id         TEXT primary key,
    login      TEXT not null,
    first_name  TEXT,
    last_name   TEXT,
    name       TEXT,
    email      TEXT,
    image_url   TEXT,
    activated  boolean   default false,
    roles      TEXT[]    DEFAULT '{}',
    last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO app_user (id,login,first_name,last_name)
VALUES ('007','system','James','Bond');

CREATE INDEX ON app_user (login);
CREATE INDEX ON app_user (email);
CREATE INDEX ON app_user USING gin (roles);
