INSERT INTO messages (CONTENIDO, Id_EMISOR, Id_RECEPTOR, FECHA_ENVIO, LEIDO) VALUES
('Hola, ¿viste el último cambio en el repositorio?', 1, 2, NOW(), 0),
('Sí, lo reviso en un momento. ¡Gracias!', 2, 1, NOW(), 1),
('¿Mañana nos reunimos para el deploy?', 1, 3, NOW(), 0);