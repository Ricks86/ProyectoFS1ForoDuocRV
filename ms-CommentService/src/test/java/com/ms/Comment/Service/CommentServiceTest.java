package com.ms.Comment.Service;

import com.ms.Comment.Client.NotificationClient;
import com.ms.Comment.Client.UserClient;
import com.ms.Comment.DTOs.CommentCreateDTO;
import com.ms.Comment.DTOs.CommentResponseDTO;
import com.ms.Comment.DTOs.NotificationCreateDTO;
import com.ms.Comment.DTOs.UserDTO;
import com.ms.Comment.Model.*;
import com.ms.Comment.Repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {
    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserClient userClient;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private CommentService commentService;

    private Comment mockComment;
    private UserDTO mockUserDTO;
    private CommentCreateDTO createRequest;
    private final String MOCK_TOKEN = "Bearer token123";

    @BeforeEach
    void setUp() {
        createRequest = new CommentCreateDTO();
        createRequest.setContent("Este es un comentario de prueba");
        createRequest.setPostId(50L);

        mockComment = Comment.builder()
                .id(1L)
                .content("Este es un comentario de prueba")
                .postId(50L)
                .userId(100L)
                .createdAt(LocalDateTime.now())
                .build();

        mockUserDTO = new UserDTO(100L, "DuckyProtocol", "El Pato");
    }

    @Test
    void testCrearComentario_OK() {
        Mockito.when(commentRepository.save(any(Comment.class))).thenReturn(mockComment);
        Mockito.when(userClient.obtenerUsuarioPorId(eq(100L), eq(MOCK_TOKEN))).thenReturn(mockUserDTO);

        CommentResponseDTO response = commentService.crearComentario(createRequest, 100L, MOCK_TOKEN);

        assertNotNull(response);
        assertEquals("Este es un comentario de prueba", response.getContent());
        assertEquals("DuckyProtocol", response.getAutor().getUsername());

        Mockito.verify(commentRepository, Mockito.times(1)).save(any(Comment.class));
        Mockito.verify(notificationClient, Mockito.times(1)).enviarNotificacion(any(NotificationCreateDTO.class), anyString());
    }

    @Test
    void testCrearComentario_200SinAutoNotificacion() {
        Mockito.when(commentRepository.save(any(Comment.class))).thenReturn(mockComment);
        Mockito.when(userClient.obtenerUsuarioPorId(eq(50L), eq(MOCK_TOKEN))).thenReturn(mockUserDTO);

        CommentResponseDTO response = commentService.crearComentario(createRequest, 50L, MOCK_TOKEN);

        assertNotNull(response);
        Mockito.verify(notificationClient, Mockito.never()).enviarNotificacion(any(), anyString());
    }

    @Test
    void testCrearComentario_ErrorMSNotification() {
        Mockito.when(commentRepository.save(any(Comment.class))).thenReturn(mockComment);
        Mockito.when(userClient.obtenerUsuarioPorId(eq(100L), eq(MOCK_TOKEN))).thenReturn(mockUserDTO);

        Mockito.doThrow(new RuntimeException("ms-Notification caído"))
                .when(notificationClient).enviarNotificacion(any(), anyString());

        CommentResponseDTO response = commentService.crearComentario(createRequest, 100L, MOCK_TOKEN);

        assertNotNull(response);
        assertEquals("DuckyProtocol", response.getAutor().getUsername());
    }

    @Test
    void testCrearComentario_ErrorMSUsers() {
        Mockito.when(commentRepository.save(any(Comment.class))).thenReturn(mockComment);

        Mockito.when(userClient.obtenerUsuarioPorId(eq(100L), eq(MOCK_TOKEN)))
                .thenThrow(new RuntimeException("ms-User caído"));

        CommentResponseDTO response = commentService.crearComentario(createRequest, 100L, MOCK_TOKEN);

        assertNotNull(response);
        assertEquals("Usuario Desconocido", response.getAutor().getUsername());
    }

    @Test
    void testObtenerComentariosPorPostId_OK() {
        Mockito.when(commentRepository.findByPostIdOrderByCreatedAtAsc(50L)).thenReturn(List.of(mockComment));
        Mockito.when(userClient.obtenerUsuarioPorId(eq(100L), eq(MOCK_TOKEN))).thenReturn(mockUserDTO);

        List<CommentResponseDTO> responseList = commentService.obtenerComentariosPorPostId(50L, MOCK_TOKEN);

        assertFalse(responseList.isEmpty());
        assertEquals(1, responseList.size());
        assertEquals("DuckyProtocol", responseList.get(0).getAutor().getUsername());
    }

    @Test
    void testObtenerAutorPorId_OK() {
        Mockito.when(commentRepository.findById(1L)).thenReturn(Optional.of(mockComment));

        Long autorId = commentService.obtenerAutorPorId(1L);

        assertEquals(100L, autorId);
    }

    @Test
    void testObtenerAutorPorId_NotFound() {
        Mockito.when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            commentService.obtenerAutorPorId(99L);
        });

        assertTrue(exception.getMessage().contains("Comentario no encontrado con ID: 99"));
    }
}