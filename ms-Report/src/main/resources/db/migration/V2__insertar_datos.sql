INSERT INTO reports (reporter_id, reported_entity_type, reported_entity_id, reason, status, created_at) VALUES
    (1, 'POST', 101, 'Contenido inapropiado y lenguaje ofensivo hacia la comunidad.', 'PENDING', CURRENT_TIMESTAMP),
    (2, 'COMMENT', 504, 'Spam repetitivo promoviendo sitios externos de dudosa procedencia.', 'RESOLVED', CURRENT_TIMESTAMP),
    (3, 'USER', 42, 'Suplantación de identidad. El usuario finge ser un administrador del foro.', 'PENDING', CURRENT_TIMESTAMP),
    (4, 'POST', 205, 'Publicación que contiene enlaces a descargas de software malicioso.', 'RESOLVED', CURRENT_TIMESTAMP),
    (1, 'COMMENT', 809, 'Acoso directo y hostigamiento hacia otro participante del hilo.', 'PENDING', CURRENT_TIMESTAMP);