CREATE TABLE user_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    bio VARCHAR(500),
    avatar_url VARCHAR(255),
    reputation_level INT DEFAULT 0,
    followers_count INT DEFAULT 0,
    following_count INT DEFAULT 0
);