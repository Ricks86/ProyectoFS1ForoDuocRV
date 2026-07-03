package com.ms.Post.Controller;


import com.ms.Post.DTOs.PostCreateDTO;
import com.ms.Post.DTOs.PostFeedDTO;
import com.ms.Post.DTOs.PostResponseDTO;
import com.ms.Post.Security.JwtUtil;
import com.ms.Post.Service.AuditService;
import com.ms.Post.Service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST el MS Posts.
 * <p>
 * Orquesta las peticiones HTTP relacionadas con los post,
 * desde su creación hasta la consulta del feed el filtrado por fecha.
 * Su responsabilidad incluye extraer de forma segura la identidad del emisor mediante el token JWT,
 * delegar las reglas de negocio y consistencia a la capa de Servicio, y emitir
 * trazabilidad asíncrona hacia el microservicio de Auditoría.
 * <p>
 * NOTA: La especificación técnica detallada de los endpoints, payloads,
 * parámetros de paginación y códigos de estado HTTP se encuentra documentada
 * y expuesta de forma automatizada mediante Swagger UI.
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor

@Tag(   name = "Publicaciones",
        description = "Controlador principal para la creación, consulta y filtrado de hilos de discusión (Posts)")

public class PostController {

    private final PostService postService;
    private final JwtUtil jwtUtil;
    private final AuditService auditoriaService;

    @PostMapping
    @Operation(
            summary = "Crear una nueva publicación",
            description = "Registra un nuevo Post en la base de datos. Extrae el ID del autor criptográficamente desde el token JWT e impacta asíncronamente al servicio de Auditoría."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Publicación forjada y guardada con éxito."),
            @ApiResponse(responseCode = "403", description = "Acceso denegado. Token JWT ausente, alterado o expirado.")
    })
    public ResponseEntity<PostResponseDTO> crearPost(
            @Valid @RequestBody PostCreateDTO request,
            @RequestHeader("Authorization") String token) {

        Long idUsuarioLogueado = jwtUtil.extractUserId(token);

        PostResponseDTO nuevoPost = postService.crearPost(request, idUsuarioLogueado, token);

        auditoriaService.registrarLog(
                idUsuarioLogueado,
                "CREATE_POST",
                "Post publicado exitosamente con ID [" + nuevoPost.getId() + "] y título: '" + nuevoPost.getTitulo() + "'"
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoPost);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar una publicación por su ID",
            description = "Recupera los datos detallados de un Post específico y orquesta una consulta interna vía Feign hacia ms-User para adjuntar el perfil del autor."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Publicación encontrada y mapeada."),
            @ApiResponse(responseCode = "404", description = "El ID de la publicación no existe en el sistema."),
            @ApiResponse(responseCode = "403", description = "Acceso denegado. Token JWT ausente, alterado o expirado.")
    })
    public ResponseEntity<PostResponseDTO> obtenerPorId(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(postService.obtenerPostPorId(id, token));
    }

    @GetMapping("/filtrar/antes-de")
    @Operation(
            summary = "Filtrar publicaciones antiguas",
            description = "Retorna una lista de publicaciones cuya fecha de creación sea estrictamente anterior a la fecha provista."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de publicaciones recuperada (puede retornar un array vacío)."),
            @ApiResponse(responseCode = "403", description = "Acceso denegado. Token JWT ausente, alterado o expirado.")
    })
    public ResponseEntity<List<PostResponseDTO>> obtenerAntesDe(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestHeader("Authorization") String token) {

        LocalDateTime fechaLimite = fecha.atStartOfDay();
        return ResponseEntity.ok(postService.obtenerPostsCreadosAntesDe(fechaLimite, token));
    }

    @GetMapping("/usuario/{username}")
    @Operation(
            summary = "Obtener publicaciones de un usuario específico",
            description = "Recupera la lista histórica de publicaciones creadas por un nombre de usuario (username) en particular."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista histórica devuelta con éxito."),
            @ApiResponse(responseCode = "404", description = "El username especificado no existe."),
            @ApiResponse(responseCode = "403", description = "Acceso denegado. Token JWT ausente, alterado o expirado.")

    })
    public ResponseEntity<List<PostResponseDTO>> obtenerPorUsername(
            @PathVariable String username,
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(postService.obtenerPostsPorUsername(username, token));
    }

    @GetMapping("/feed")
    @Operation(
            summary = "Obtener el Feed global paginado",
            description = "Retorna una página de publicaciones diseñada para el consumo de la vista principal del foro."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de contenido devuelta con metadatos de paginación."),
            @ApiResponse(responseCode = "403", description = "Acceso denegado. Token JWT ausente, alterado o expirado.")
    })
    public ResponseEntity<Page<PostFeedDTO>> obtenerFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("Authorization") String token) {

        Pageable pageable = PageRequest.of(page, size);
        Page<PostFeedDTO> feed = postService.obtenerFeedPaginado(pageable, token);

        return ResponseEntity.ok(feed);
    }

    @GetMapping("/{postId}/author-id")
    @Operation(
            summary = "Endpoint de Comunicación Interna: Obtener ID del Autor",
            description = "Ruta interna de alta velocidad **(Uso exclusivo para OpenFeign)**. Permite a microservicios como `ms-Interaction` o `ms-Comment` saber instantáneamente quién es el dueño de un post para detonar sus lógicas de negocio o notificaciones."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "ID del autor devuelto exitosamente."),
            @ApiResponse(responseCode = "404", description = "Publicación no encontrada.")
    })
    public ResponseEntity<Long> getAuthorIdByPostId(@PathVariable Long postId) {
        Long authorId = postService.getAuthorIdByPostId(postId);
        return ResponseEntity.ok(authorId);
    }
}
