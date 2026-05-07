CREATE TABLE messages (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          CONTENIDO VARCHAR(500) NOT NULL,
                          Id_EMISOR INT NOT NULL,
                          Id_RECEPTOR INT NOT NULL,
                          FECHA_ENVIO DATETIME,
                          LEIDO TINYINT(1) DEFAULT 1
);