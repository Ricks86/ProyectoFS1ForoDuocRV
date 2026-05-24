INSERT INTO notifications (recipient_id, sender_id, type, service_origin, message, related_id, is_read, created_at) VALUES
(1, 2, 'LIKE', 'ms-Interaction', 'A x le gustó tu publicación.', 101, FALSE, CURRENT_TIMESTAMP),
(1, 3, 'COMMENT', 'ms-Comment', 'Alguien comentó tu post sobre Linux.', 101, TRUE, CURRENT_TIMESTAMP),
(2, NULL, 'SYSTEM', 'ms-User', 'Bienvenido a la plataforma. Por favor completa tu perfil.', NULL, FALSE, CURRENT_TIMESTAMP);