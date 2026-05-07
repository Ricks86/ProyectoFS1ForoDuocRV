CREATE TABLE comments (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          CONTENIDO VARCHAR(1000) NOT NULL,
                          ID_USUARIO BIGINT NOT NULL,
                          ID_POST BIGINT NOT NULL,
                          FECHA_CREACION DATETIME NOT NULL
);