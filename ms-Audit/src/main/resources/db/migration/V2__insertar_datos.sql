INSERT INTO audit_logs (nombre_servicio, accion, user_id, detalles, timestamp) VALUES
('ms-community', 'CREATE_COMMUNITY', 'rick_dev', 'Creación de la comunidad JavaDevs exitosa.', NOW()),
('ms-interaction', 'CAST_VOTE', 'cl_coding', 'Voto UPVOTE registrado en post ID: 101.', NOW()),
('ms-user', 'UPDATE_PROFILE', 'gamer_master', 'Actualización de avatar y bio.', NOW()),
('ms-report', 'RESOLVE_REPORT', 'admin_user', 'Reporte #45 cerrado como improcedente.', NOW());