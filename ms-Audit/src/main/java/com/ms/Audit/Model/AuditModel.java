package com.ms.Audit.Model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name= "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombreServicio;

    @Column(nullable = false)
    private String accion;

    private String userId;

    @Column(columnDefinition = "TEXT")
    private String detalles;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void alCrear() {
        this.timestamp = LocalDateTime.now();
    }
}
