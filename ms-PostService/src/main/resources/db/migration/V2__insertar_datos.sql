INSERT INTO posts (TITULO, CONTENIDO, ID_USUARIO, ID_COMUNIDAD, FECHA_CREACION) VALUES(
    'Mi primer post en el foro', 'Hola a todos, este es el inicio de un gran foro desarrollado en Linux.', 1, 10, NOW()),
    ('Duda con Spring Boot y Docker', '¿Alguien sabe cómo configurar correctamente el restart policy en el compose?', 2, 10, NOW()),
    ('Error de conexión en MySQL', 'Me está lanzando un Connection Timeout al levantar el contenedor de base de datos.', 1, NULL, NOW()),
    ('Guía rápida de WebClient', 'Aquí les dejo un pequeño tip de cómo usar WebClient con .block() de forma síncrona.', 3, 12, NOW()),
    ('Contrato de DTOs del equipo', 'Muchachos, recuerden revisar los campos de UserDTO para que no rompa la comunicación.', 2, 10, NOW());