package com.ms.Messasing.Controller;

import com.ms.Messasing.Model.MessageCreateDTO;
import com.ms.Messasing.Model.MessageResponseDTO;
import com.ms.Messasing.Model.UserDTO;
import com.ms.Messasing.Security.JwtUtil;
import com.ms.Messasing.Service.AuditService;
import com.ms.Messasing.Service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageControllerTest {
    @Mock
    private MessageService messageService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuditService auditoriaService;

    @InjectMocks
    private MessageController messageController;

    private MessageCreateDTO request;
    private MessageResponseDTO responseDTO;
    private final String falsoToken = "Bearer token.jwt.valido";
    private final String receptor = "UserDestino";
    private final Long emisorId = 10L;

    @BeforeEach
    void setUp() {
        request = new MessageCreateDTO();
        request.setContenido("Hola, ¿cómo estás?");

        UserDTO usuario = UserDTO.builder().id(emisorId).username("Emisor").build();

        responseDTO = MessageResponseDTO.builder()
                .id(1L)
                .contenido("Hola, ¿cómo estás?")
                .fechaEnvio(LocalDateTime.now())
                .leido(false)
                .emisor(usuario)
                .receptor(usuario)
                .build();
    }

    @Test
    void enviarMensaje_OK() {
        when(jwtUtil.extractUserId(falsoToken)).thenReturn(emisorId);
        when(messageService.enviarMensajePorUsername(receptor, request, emisorId, falsoToken))
                .thenReturn(responseDTO);

        ResponseEntity<MessageResponseDTO> response = messageController.enviarMensaje(receptor, request, falsoToken);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals(emisorId, response.getBody().getEmisor().getId());

        verify(auditoriaService, times(1)).registrarLog(
                emisorId,
                "SEND_MESSAGE",
                "Mensaje privado enviado exitosamente al usuario: " + receptor
        );
    }
}
