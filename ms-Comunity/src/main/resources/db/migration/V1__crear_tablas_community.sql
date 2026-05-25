-- Tabla principal de Comunidades
CREATE TABLE communities (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             name VARCHAR(50) NOT NULL UNIQUE,
                             description VARCHAR(500) NOT NULL,
                             creator_id BIGINT NOT NULL,
                             community_access VARCHAR(255) NOT NULL,
                             member_count INT NOT NULL DEFAULT 0,
                             created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE community_members (
                                   community_id BIGINT NOT NULL,
                                   user_id BIGINT NOT NULL,
                                   CONSTRAINT fk_community_members_community FOREIGN KEY (community_id) REFERENCES communities(id) ON DELETE CASCADE
);

CREATE INDEX idx_communities_creator ON communities(creator_id);
CREATE INDEX idx_community_members_user ON community_members(user_id);