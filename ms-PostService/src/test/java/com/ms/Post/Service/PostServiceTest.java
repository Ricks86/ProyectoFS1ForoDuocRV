package com.ms.Post.Service;

import com.ms.Post.Client.UserClient;
import com.ms.Post.Model.Post;
import com.ms.Post.Model.PostCreateDTO;
import com.ms.Post.Model.PostFeedDTO;
import com.ms.Post.Model.PostResponseDTO;
import com.ms.Post.Model.UserDTO;
import com.ms.Post.Repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {
    @Mock
    private PostRepository postRepository;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private PostService postService;

    private Post mockPost;
    private UserDTO mockUserDTO;
    private final String MOCK_TOKEN = "Bearer token123";

    @BeforeEach
    void setUp() {
        mockPost = Post.builder()
                .id(1L)
                .titulo("Título de prueba")
                .contenido("Contenido de prueba")
                .idUsuario(100L)
                .idComunidad(10L)
                .fechaCreacion(LocalDateTime.now())
                .build();

        mockUserDTO = new UserDTO(100L, "testuser", "Alias Test");
    }

    @Test
    void testCrearPost_OK() {
        PostCreateDTO createDTO = new PostCreateDTO();
        createDTO.setTitulo("Título de prueba");
        createDTO.setContenido("Contenido de prueba");
        createDTO.setIdComunidad(10L);

        Mockito.when(postRepository.save(any(Post.class))).thenReturn(mockPost);
        Mockito.when(userClient.obtenerUsuarioPorId(eq(100L), eq(MOCK_TOKEN))).thenReturn(mockUserDTO);

        PostResponseDTO response = postService.crearPost(createDTO, 100L, MOCK_TOKEN);

        assertNotNull(response);
        assertEquals("Título de prueba", response.getTitulo());
        assertEquals("testuser", response.getAutor().getUsername());
        Mockito.verify(postRepository, Mockito.times(1)).save(any(Post.class));
    }

    @Test
    void testObtenerPostPorId_OK() {
        Mockito.when(postRepository.findById(1L)).thenReturn(Optional.of(mockPost));
        Mockito.when(userClient.obtenerUsuarioPorId(eq(100L), eq(MOCK_TOKEN))).thenReturn(mockUserDTO);

        PostResponseDTO response = postService.obtenerPostPorId(1L, MOCK_TOKEN);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("testuser", response.getAutor().getUsername());
    }

    @Test
    void testObtenerPostPorId_NotFound() {
        Mockito.when(postRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            postService.obtenerPostPorId(99L, MOCK_TOKEN);
        });

        assertTrue(exception.getMessage().contains("El post con ID 99 no existe"));
    }

    @Test
    void testObtenerPostsCreadosAntesDe_OK() {
        LocalDateTime fecha = LocalDateTime.now().plusDays(1);
        Mockito.when(postRepository.findByFechaCreacionBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(mockPost));
        Mockito.when(userClient.obtenerUsuarioPorId(eq(100L), eq(MOCK_TOKEN))).thenReturn(mockUserDTO);

        List<PostResponseDTO> response = postService.obtenerPostsCreadosAntesDe(fecha, MOCK_TOKEN);

        assertFalse(response.isEmpty());
        assertEquals(1, response.size());
        assertEquals(1L, response.get(0).getId());
    }

    @Test
    void testObtenerPostsPorUsername_OK() {
        Mockito.when(userClient.obtenerUsuarioPorUsername(eq("testuser"), eq(MOCK_TOKEN))).thenReturn(mockUserDTO);
        Mockito.when(postRepository.findByIdUsuario(100L)).thenReturn(List.of(mockPost));

        List<PostResponseDTO> response = postService.obtenerPostsPorUsername("testuser", MOCK_TOKEN);

        assertFalse(response.isEmpty());
        assertEquals("Título de prueba", response.get(0).getTitulo());
    }

    @Test
    void testObtenerPostsPorUsername_NotFound() {
        Mockito.when(userClient.obtenerUsuarioPorUsername(eq("usuarioFantasma"), eq(MOCK_TOKEN)))
                .thenThrow(new RuntimeException("Error 500 del servidor ms-User"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            postService.obtenerPostsPorUsername("usuarioFantasma", MOCK_TOKEN);
        });

        assertTrue(exception.getMessage().contains("El usuario '@usuarioFantasma' no existe en el sistema"));
    }

    @Test
    void testObtenerFeedPaginado_OK() {
        Page<Post> page = new PageImpl<>(List.of(mockPost));
        Mockito.when(postRepository.findAllByOrderByFechaCreacionAsc(any(PageRequest.class))).thenReturn(page);
        Mockito.when(userClient.obtenerUsuarioPorId(eq(100L), eq(MOCK_TOKEN))).thenReturn(mockUserDTO);

        Page<PostFeedDTO> response = postService.obtenerFeedPaginado(PageRequest.of(0, 10), MOCK_TOKEN);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals("Título de prueba", response.getContent().get(0).getTitulo());
    }

    @Test
    void testObtenerAutor_403() {
        Mockito.when(postRepository.findById(1L)).thenReturn(Optional.of(mockPost));
        Mockito.when(userClient.obtenerUsuarioPorId(eq(100L), eq(MOCK_TOKEN)))
                .thenThrow(new RuntimeException("TimeOut del ms-User"));

        PostResponseDTO response = postService.obtenerPostPorId(1L, MOCK_TOKEN);

        assertNotNull(response);
        assertEquals("Usuario Temporal", response.getAutor().getUsername());
        assertEquals("Alias No Disponible", response.getAutor().getAlias());
    }

    @Test
    void testGetAuthorIdByPostId_OK() {
        Mockito.when(postRepository.findById(1L)).thenReturn(Optional.of(mockPost));

        Long authorId = postService.getAuthorIdByPostId(1L);

        assertEquals(100L, authorId);
    }
}

