CREATE TABLE IF NOT EXISTS region (
                                    id SERIAL PRIMARY KEY,
                                    code VARCHAR(10) NOT NULL,
                                    parentCode VARCHAR(10) NOT NULL,
                                    name VARCHAR(255) NOT NULL
                                    --description TEXT,
                                    --created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    --updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ;

-- insert into region (code,parentCode,name) values ('de','eu','Germany')
