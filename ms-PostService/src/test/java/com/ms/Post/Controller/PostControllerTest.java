package com.ms.Post.Controller;

import com.ms.Post.Model.PostCreateDTO;
import com.ms.Post.Model.PostFeedDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

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
    void crearPost_OK() {
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

    @Test
    void obtenerFeed_OK() {
        int page = 0;
        int size = 5;
        Pageable pageable = PageRequest.of(page, size);
        List<PostFeedDTO> listaPosts = List.of(new PostFeedDTO(), new PostFeedDTO());
        Page<PostFeedDTO> paginaSimulada = new PageImpl<>(listaPosts, pageable, listaPosts.size());

        when(postService.obtenerFeedPaginado(any(Pageable.class), eq(falsoToken))).thenReturn(paginaSimulada);

        ResponseEntity<Page<PostFeedDTO>> response = postController.obtenerFeed(page, size, falsoToken);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getTotalElements());

        verify(postService, times(1)).obtenerFeedPaginado(any(Pageable.class), eq(falsoToken));
    }
}
