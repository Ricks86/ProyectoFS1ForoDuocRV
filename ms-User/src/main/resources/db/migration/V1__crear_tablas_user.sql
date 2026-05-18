CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       auth_id BIGINT NOT NULL UNIQUE,
                       username VARCHAR(64) NOT NULL UNIQUE,
                       email VARCHAR(255) NOT NULL,
                       bio VARCHAR(500) NULL,
                       alias VARCHAR(100) NULL,
                       birthday VARCHAR(50) NULL,
                       avatar_url VARCHAR(255) NULL,
                       reputation_level INT NOT NULL DEFAULT 0,
                       followers_count INT NOT NULL DEFAULT 0,
                       following_count INT NOT NULL DEFAULT 0,
                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);