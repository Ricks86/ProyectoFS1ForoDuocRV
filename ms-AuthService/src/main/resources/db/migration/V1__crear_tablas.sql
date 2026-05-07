CREATE TABLE user_auth (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           nombre_user VARCHAR(32) NOT NULL UNIQUE,
                           email VARCHAR(100) NOT NULL UNIQUE,
                           password VARCHAR(255) NOT NULL,
                           created_at DATETIME NOT NULL
);