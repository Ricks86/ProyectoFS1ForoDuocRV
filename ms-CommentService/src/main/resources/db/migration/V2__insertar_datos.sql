INSERT INTO comments (CONTENIDO, ID_USUARIO, ID_POST, FECHA_CREACION) VALUES
    ('¡Excelente iniciativa! Hacía falta un espacio así para la comunidad informática.', 2, 1, NOW()),
    ('Buenísimo, sintoniza totalmente con el estilo Linux.', 3, 1, NOW());

INSERT INTO comments (CONTENIDO, ID_USUARIO, ID_POST, FECHA_CREACION) VALUES
    ('Me pasaba lo mismo en Nobara. Revisa si el puerto 3306 ya está ocupado por un servicio local.', 2, 3, NOW()),
    ('Asegúrate de que el contenedor de la app y el de MySQL estén en la misma Docker Network.', 3, 3, NOW()),
    ('¡Gracias muchachos! Mapeé bien la red en el docker-compose y levantó de inmediato.', 1, 3, NOW());