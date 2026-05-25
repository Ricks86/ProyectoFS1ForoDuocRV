CREATE TABLE votes (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       user_id BIGINT NOT NULL,
                       entity_type VARCHAR(50) NOT NULL,
                       entity_id BIGINT NOT NULL,
                       vote_type VARCHAR(50) NOT NULL,
                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT uk_user_entity UNIQUE (user_id, entity_type, entity_id)
);

CREATE INDEX idx_votes_entity ON votes(entity_type, entity_id);

CREATE INDEX idx_votes_user ON votes(user_id);