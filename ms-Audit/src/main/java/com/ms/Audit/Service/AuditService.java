package com.ms.Audit.Service;

import com.ms.Audit.Model.AuditModel;
import com.ms.Audit.Model.AuditRequestDto;
import com.ms.Audit.Repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {
    private final AuditRepository repositorio;

    public void crearLog(AuditRequestDto dto){
        log.info("Registrando actividad del servicio: {}",dto.nombreService());

        AuditModel logIn = AuditModel.builder()
                .nombreServicio(dto.nombreService())
                .accion(dto.accion())
                .userId(dto.userID())
                .detalles(dto.details())
                .timestamp(LocalDateTime.now())
                .build();

        repositorio.save(logIn);
    }

    public List<AuditModel>getAllLog(){
        return repositorio.findAll();
    }

    public List<AuditModel>getLogsPorNombre(String nombre){
        return repositorio.findByNombreServicio(nombre);
    }

    public List<AuditModel>getLogsPorRangoFecha(LocalDateTime start, LocalDateTime end){
        return repositorio.findByTimestampBetween(start, end);
    }

}
