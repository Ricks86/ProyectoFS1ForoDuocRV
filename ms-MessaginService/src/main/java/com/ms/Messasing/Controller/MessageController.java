package com.ms.Messasing.Controller;

import com.ms.Messasing.DTOs.BandejaItemDTO;
import com.ms.Messasing.DTOs.MessageCreateDTO;
import com.ms.Messasing.DTOs.MessageResponseDTO;
import com.ms.Messasing.Security.JwtUtil;
import com.ms.Messasing.Service.AuditService;
import com.ms.Messasing.Service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(
        name = "Mensajería Privada",
        description = "Controlador para el envío, recepción y consulta de mensajes privados entre usuarios")

public class MessageController {
    private final MessageService messageService;
    private final JwtUtil jwtUtil;
    private final AuditService auditoriaService;

    @PostMapping("/enviar/{usernameReceptor}")
    @Operation(
            summary = "Enviar mensaje privado",
            description = "Envía un mensaje privado a otro usuario identificado por su nombre de usuario. Registra la acción en el ms-Audit del sistema."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mensaje enviado exitosamente."),
            @ApiResponse(responseCode = "400", description = "Error de validación en el contenido del mensaje."),
            @ApiResponse(responseCode = "403", description = "Acceso denegado. Token JWT ausente, alterado o expirado."),
            @ApiResponse(responseCode = "404", description = "El usuario receptor no existe.")
    })
    public ResponseEntity<MessageResponseDTO> enviarMensaje(
            @PathVariable String usernameReceptor,
            @Valid @RequestBody MessageCreateDTO request,
            @RequestHeader("Authorization") String token) {

        Long idEmisorLogueado = jwtUtil.extractUserId(token);

        MessageResponseDTO respuesta = messageService.enviarMensajePorUsername(usernameReceptor, request, idEmisorLogueado, token);

        auditoriaService.registrarLog(
                idEmisorLogueado,
                "SEND_MESSAGE",
                "Mensaje privado enviado exitosamente al usuario: " + usernameReceptor
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping("/bandeja")
    @Operation(
            summary = "Obtener bandeja de entrada",
            description = "Recupera la lista de conversaciones activas o mensajes recibidos por el usuario logueado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bandeja de entrada recuperada con éxito."),
            @ApiResponse(responseCode = "403", description = "Acceso denegado. Token JWT ausente, alterado o expirado.")
    })
    public ResponseEntity<List<BandejaItemDTO>> obtenerBandeja(@RequestHeader("Authorization") String token) {

        Long idLogueado = jwtUtil.extractUserId(token);

        List<BandejaItemDTO> bandeja = messageService.obtenerBandejaEntrada(idLogueado, token);
        return ResponseEntity.ok(bandeja);
    }

    @GetMapping("/conversacion/{otroUsuario}")
    @Operation(
            summary = "Obtener historial de conversación",
            description = "Recupera todos los mensajes intercambiados entre el usuario logueado y otro usuario específico."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial recuperado exitosamente."),
            @ApiResponse(responseCode = "403", description = "Acceso denegado. Token JWT ausente, alterado o expirado."),
            @ApiResponse(responseCode = "404", description = "El usuario solicitado no existe.")
    })
    public ResponseEntity<List<MessageResponseDTO>> obtenerChat(
            @PathVariable String otroUsuario,
            @RequestHeader("Authorization") String token) {

        Long idLogueado = jwtUtil.extractUserId(token);

        List<MessageResponseDTO> conversacion = messageService.obtenerConversacion(idLogueado, otroUsuario, token);

        return ResponseEntity.ok(conversacion);
    }
}
