package com.ms.Messasing.Controller;

import com.ms.Messasing.Model.MessageRequestDTO;
import com.ms.Messasing.Model.MessageResponseDTO;
import com.ms.Messasing.Service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController {
    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<MessageResponseDTO> enviarMensaje(@Valid @RequestBody MessageRequestDTO dto) {
        log.info("Iniciando envio de mensaje des el usuario {} hacia {}", dto.getEmisorId(), dto.getReceptorId());

        MessageResponseDTO response = messageService.enviarMessage(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MessageResponseDTO>> obtenerMensajes(@PathVariable Long userId) {
        log.info("Iniciando obtener mensajes por usuario {}", userId);
        List<MessageResponseDTO> mensajes = messageService.obtenerMessages(userId);

        if (mensajes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(mensajes);
    }
}
