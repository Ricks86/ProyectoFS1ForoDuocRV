-- 1. Insertar comunidades base
INSERT INTO communities (name, description, creator_id, community_access, member_count, created_at) VALUES
    ('Desarrolladores Spring Boot', 'Comunidad exclusiva para discutir arquitectura, microservicios y patrones en Spring.', 1, 'SPRING2026', 2, CURRENT_TIMESTAMP),
    ('Usuarios Nobara Linux', 'Espacio para troubleshooting de drivers Nvidia, Wayland y gaming en Linux.', 2, 'PENGUIN_SECURE', 3, CURRENT_TIMESTAMP),
    ('Mesa de Rol y Fantasía', 'Grupo cerrado para organizar campañas, discutir lore y homebrew rules.', 1, 'D20_CRIT_HIT', 1, CURRENT_TIMESTAMP);


INSERT INTO community_members (community_id, user_id) VALUES
    (1, 1),
    (1, 2);

INSERT INTO community_members (community_id, user_id) VALUES
    (2, 2),
    (2, 1),
    (2, 3);

INSERT INTO community_members (community_id, user_id) VALUES
    (3, 1);