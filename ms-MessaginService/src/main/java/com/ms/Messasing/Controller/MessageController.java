package com.ms.Messasing.Controller;

import com.ms.Messasing.Model.MessageCreatetDTO;
import com.ms.Messasing.Model.MessageResponseDTO;
import com.ms.Messasing.Model.UserDTO;
import com.ms.Messasing.Security.JwtUtil;
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
    private final JwtUtil jwtUtil;

    @PostMapping("/enviar/{usernameReceptor}")
    public ResponseEntity<MessageResponseDTO> enviarMensaje(
            @PathVariable String usernameReceptor,
            @Valid @RequestBody MessageCreatetDTO request,
            @RequestHeader("Authorization") String token) {

        Long idEmisorLogueado = jwtUtil.extractUserId(token);

        MessageResponseDTO respuesta = messageService.enviarMensajePorUsername(usernameReceptor, request, idEmisorLogueado);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping("/bandeja")
    public ResponseEntity<List<UserDTO>> obtenerBandeja(@RequestHeader("Authorization") String token) {

        Long idLogueado = jwtUtil.extractUserId(token);

        List<UserDTO> bandeja = messageService.obtenerBandejaEntrada(idLogueado);
        return ResponseEntity.ok(bandeja);
    }

    @GetMapping("/conversacion/{idOtroUsuario}")
    public ResponseEntity<List<MessageResponseDTO>> obtenerChat(
            @PathVariable Long idOtroUsuario,
            @RequestHeader("Authorization") String token) {

        Long idLogueado = jwtUtil.extractUserId(token);

        List<MessageResponseDTO> conversacion = messageService.obtenerConversacion(idLogueado, idOtroUsuario);
        return ResponseEntity.ok(conversacion);
    }
}
