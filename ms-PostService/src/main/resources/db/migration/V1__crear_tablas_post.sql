CREATE TABLE posts (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       TITULO VARCHAR(64) NOT NULL,
                       CONTENIDO VARCHAR(2000) NOT NULL,
                       ID_USUARIO BIGINT NOT NULL,
                       ID_COMUNIDAD BIGINT NOT NULL,
                       FECHA_CREACION DATETIME NOT NULL
);