CREATE TABLE audit_logs (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            nombre_servicio VARCHAR(255) NOT NULL,
                            accion VARCHAR(255) NOT NULL,
                            user_id VARCHAR(255),
                            detalles TEXT,
                            timestamp DATETIME NOT NULL
);