CREATE TABLE IF NOT EXISTS place (
              --id SERIAL PRIMARY KEY,
              id VARCHAR(255) primary key,
              name VARCHAR(255) NOT NULL,
            summary TEXT
    --description TEXT,
    --created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    --updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ;

