package com.ms.Messasing.Service;

import com.ms.Messasing.Client.NotificationClient;
import com.ms.Messasing.Client.UserClient;
import com.ms.Messasing.DTOs.*;
import com.ms.Messasing.Model.*;
import com.ms.Messasing.Repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {
    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserClient userClient;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private MessageService messageService;

    private MessageCreateDTO createRequest;
    private Message mockMessage;
    private UserDTO emisorDTO;
    private UserDTO receptorDTO;
    private final String MOCK_TOKEN = "Bearer token123";

    @BeforeEach
    void setUp() {
        createRequest = new MessageCreateDTO();
        createRequest.setContenido("Hola, ¿cómo estás?");

        mockMessage = Message.builder()
                .id(1L)
                .contenido("Hola, ¿cómo estás?")
                .idEmisor(10L)
                .idReceptor(20L)
                .fechaEnvio(LocalDateTime.now())
                .leido(false)
                .build();

        emisorDTO = new UserDTO(10L, "usuarioEmisor", "Alias Emisor");
        receptorDTO = new UserDTO(20L, "usuarioReceptor", "Alias Receptor");
    }

    @Test
    void testEnviarMensajePorUsername_ExitoYNotificacion() {
        Mockito.when(userClient.obtenerUsuarioPorUsername(eq("usuarioReceptor"), eq(MOCK_TOKEN))).thenReturn(receptorDTO);
        Mockito.when(userClient.obtenerUsuarioPorId(eq(10L), eq(MOCK_TOKEN))).thenReturn(emisorDTO);
        Mockito.when(messageRepository.save(any(Message.class))).thenReturn(mockMessage);

        MessageResponseDTO response = messageService.enviarMensajePorUsername("usuarioReceptor", createRequest, 10L, MOCK_TOKEN);

        assertNotNull(response);
        assertEquals("Hola, ¿cómo estás?", response.getContenido());
        assertEquals("usuarioEmisor", response.getEmisor().getUsername());
        Mockito.verify(messageRepository, Mockito.times(1)).save(any(Message.class));
        Mockito.verify(notificationClient, Mockito.times(1)).enviarNotificacion(any(NotificationCreateDTO.class), anyString());
    }

    @Test
    void testEnviarMensajePorUsername_FallaReceptorNoExiste() {
        Mockito.when(userClient.obtenerUsuarioPorUsername(eq("fantasma"), eq(MOCK_TOKEN)))
                .thenThrow(new RuntimeException("Usuario no encontrado"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            messageService.enviarMensajePorUsername("fantasma", createRequest, 10L, MOCK_TOKEN);
        });

        assertEquals("El usuario '@fantasma' no existe en el sistema.", exception.getMessage());
        Mockito.verify(messageRepository, Mockito.never()).save(any(Message.class));
    }

    @Test
    void testEnviarMensajePorUsername_FallaNotificacionResiliente() {
        Mockito.when(userClient.obtenerUsuarioPorUsername(eq("usuarioReceptor"), eq(MOCK_TOKEN))).thenReturn(receptorDTO);
        Mockito.when(userClient.obtenerUsuarioPorId(eq(10L), eq(MOCK_TOKEN))).thenReturn(emisorDTO);
        Mockito.when(messageRepository.save(any(Message.class))).thenReturn(mockMessage);

        Mockito.doThrow(new RuntimeException("ms-Notification caído"))
                .when(notificationClient).enviarNotificacion(any(), anyString());

        MessageResponseDTO response = messageService.enviarMensajePorUsername("usuarioReceptor", createRequest, 10L, MOCK_TOKEN);

        assertNotNull(response);
        assertEquals("Hola, ¿cómo estás?", response.getContenido());
    }

    @Test
    void testObtenerBandejaEntrada_Exito() {
        Object[] fila = new Object[]{10L, 2L, LocalDateTime.now()};
        List<Object[]> resultadosMock = new ArrayList<>();
        resultadosMock.add(fila);

        Mockito.when(messageRepository.getResumenBandeja(20L)).thenReturn(resultadosMock);
        Mockito.when(userClient.obtenerUsuarioPorId(eq(10L), eq(MOCK_TOKEN))).thenReturn(emisorDTO);

        List<BandejaItemDTO> bandeja = messageService.obtenerBandejaEntrada(20L, MOCK_TOKEN);

        assertFalse(bandeja.isEmpty());
        assertEquals(2L, bandeja.get(0).getMensajesSinLeer());
        assertEquals("usuarioEmisor", bandeja.get(0).getUsuario().getUsername());
    }

    @Test
    void testObtenerBandejaEntrada_FallaUserClient_Fallback() {
        Object[] fila = new Object[]{99L, 1L, LocalDateTime.now()};
        List<Object[]> resultadosMock = new ArrayList<>();
        resultadosMock.add(fila);

        Mockito.when(messageRepository.getResumenBandeja(20L)).thenReturn(resultadosMock);
        Mockito.when(userClient.obtenerUsuarioPorId(eq(99L), eq(MOCK_TOKEN)))
                .thenThrow(new RuntimeException("ms-User caído"));

        List<BandejaItemDTO> bandeja = messageService.obtenerBandejaEntrada(20L, MOCK_TOKEN);

        assertFalse(bandeja.isEmpty());
        assertEquals("Usuario Temporal", bandeja.get(0).getUsuario().getUsername());
    }

    @Test
    void testObtenerConversacion_Exito() {
        Mockito.when(userClient.obtenerUsuarioPorUsername(eq("usuarioReceptor"), eq(MOCK_TOKEN))).thenReturn(receptorDTO);
        Mockito.when(userClient.obtenerUsuarioPorId(eq(10L), eq(MOCK_TOKEN))).thenReturn(emisorDTO);
        Mockito.when(messageRepository.findConversacionCompleta(10L, 20L)).thenReturn(List.of(mockMessage));

        Mockito.doNothing().when(messageRepository).marcarMensajesComoLeidos(20L, 10L);

        List<MessageResponseDTO> conversacion = messageService.obtenerConversacion(10L, "usuarioReceptor", MOCK_TOKEN);

        assertFalse(conversacion.isEmpty());
        assertEquals("Hola, ¿cómo estás?", conversacion.get(0).getContenido());
        assertEquals("usuarioEmisor", conversacion.get(0).getEmisor().getUsername());

        Mockito.verify(messageRepository, Mockito.times(1)).marcarMensajesComoLeidos(20L, 10L);
    }
}
