package com.ms.Comment.Controller;

import com.ms.Comment.DTOs.CommentCreateDTO;
import com.ms.Comment.DTOs.CommentResponseDTO;
import com.ms.Comment.DTOs.UserDTO;
import com.ms.Comment.Security.JwtUtil;
import com.ms.Comment.Service.AuditService;
import com.ms.Comment.Service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentControllerTest {
    @Mock
    private CommentService commentService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuditService auditoriaService;

    @InjectMocks
    private CommentController commentController;

    private CommentCreateDTO request;
    private CommentResponseDTO responseDTO;
    private final String falsoToken = "Bearer eyJhbGciOiJIUzI1NiJ9.FalsoToken";
    private final Long userId = 50L;

    @BeforeEach
    void setUp() {
        request = new CommentCreateDTO();
        request.setPostId(101L);
        request.setContent("Gran aporte, gracias!");

        UserDTO autorSimulado = new UserDTO();
        autorSimulado.setId(userId);
        autorSimulado.setUsername("DuckyProtocol");

        responseDTO = new CommentResponseDTO();
        responseDTO.setId(201L);
        responseDTO.setContent("Gran aporte, gracias!");
        responseDTO.setPostId(101L);
        responseDTO.setUserId(userId);
        responseDTO.setCreatedAt(LocalDateTime.now());
        responseDTO.setAutor(autorSimulado);
    }

    @Test
    void crearComentario_OK() {
        when(jwtUtil.extractUserId(falsoToken)).thenReturn(userId);
        when(commentService.crearComentario(request, userId, falsoToken)).thenReturn(responseDTO);

        ResponseEntity<CommentResponseDTO> response = commentController.crearComentario(request, falsoToken);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(201L, response.getBody().getId());

        assertNotNull(response.getBody().getAutor());
        assertEquals("DuckyProtocol", response.getBody().getAutor().getUsername());

        verify(auditoriaService, times(1)).registrarLog(
                userId,
                "CREATE_COMMENT",
                "Comentario ID [201] publicado en el Post ID [101]"
        );
    }

    @Test
    void obtenerComentariosPorPost_OK() {
        Long postId = 101L;
        List<CommentResponseDTO> listaSimulada = List.of(responseDTO);
        when(commentService.obtenerComentariosPorPostId(postId, falsoToken)).thenReturn(listaSimulada);

        ResponseEntity<List<CommentResponseDTO>> response = commentController.obtenerComentariosPorPost(postId, falsoToken);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());

        verify(commentService, times(1)).obtenerComentariosPorPostId(postId, falsoToken);
    }
}
