package com.ms.Post.Controller;

import com.ms.Post.Model.PostCreateDTO;
import com.ms.Post.Model.PostResponseDTO;
import com.ms.Post.Security.JwtUtil;
import com.ms.Post.Service.AuditService;
import com.ms.Post.Service.PostService;
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
public class PostControllerTest {
    @Mock
    private PostService postService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuditService auditoriaService;

    @InjectMocks
    private PostController postController;

    private PostCreateDTO request;
    private PostResponseDTO responseDTO;
    private final String falsoToken = "Bearer eyJhbGciOiJIUzI1NiJ9.FakeToken";
    private final Long idUsuarioLogueado = 50L;

    @BeforeEach
    void setUp() {
        request = new PostCreateDTO();
        request.setTitulo("¿Cómo compilar el Kernel?");
        request.setContenido("Tengo un problema con los drivers en mi distribución...");

        responseDTO = new PostResponseDTO();
        responseDTO.setId(101L);
        responseDTO.setTitulo("¿Cómo compilar el Kernel?");
        responseDTO.setContenido("Tengo un problema con los drivers en mi distribución...");
        responseDTO.setIdUsuario(idUsuarioLogueado);
        responseDTO.setFechaCreacion(LocalDateTime.now());
    }

    @Test
    void crearPost_DeberiaRetornarCreatedYRegistrarAuditoria_CuandoDatosSonValidos() {
        when(jwtUtil.extractUserId(falsoToken)).thenReturn(idUsuarioLogueado);

        when(postService.crearPost(request, idUsuarioLogueado, falsoToken)).thenReturn(responseDTO);

        ResponseEntity<PostResponseDTO> response = postController.crearPost(request, falsoToken);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(101L, response.getBody().getId());
        assertEquals("¿Cómo compilar el Kernel?", response.getBody().getTitulo());

        verify(auditoriaService, times(1)).registrarLog(
                idUsuarioLogueado,
                "CREATE_POST",
                "Post publicado exitosamente con ID [101] y título: '¿Cómo compilar el Kernel?'"
        );
    }
}
