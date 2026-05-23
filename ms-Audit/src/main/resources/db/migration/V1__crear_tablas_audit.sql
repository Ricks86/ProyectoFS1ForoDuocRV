CREATE TABLE audit_logs (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            usuario_id BIGINT NOT NULL,
                            accion VARCHAR(50) NOT NULL,
                            recurso VARCHAR(50) NOT NULL,
                            detalles VARCHAR(500) NOT NULL,
                            fecha DATETIME NOT NULL
);