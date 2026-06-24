package com.ms.Comment.Controller;

import com.ms.Comment.DTOs.CommentCreateDTO;
import com.ms.Comment.DTOs.CommentResponseDTO;
import com.ms.Comment.Security.JwtUtil;
import com.ms.Comment.Service.AuditService;
import com.ms.Comment.Service.CommentService;
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
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Slf4j

@Tag(
        name = "Comentarios",
        description = "Operaciones para la gestión de hilos de respuesta en las publicaciones")

public class CommentController {

    private final CommentService commentService;
    private final JwtUtil jwtUtil;
    private final AuditService auditoriaService;

    @PostMapping
    @Operation(
            summary = "Publicar un comentario",
            description = "Crea un nuevo comentario asociado a un post. Extrae la identidad del autor mediante el token JWT y registra la acción en el servicio de auditoría."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Comentario publicado exitosamente."),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos (ej. comentario vacío)."),
            @ApiResponse(responseCode = "403", description = "No autorizado (Token faltante o inválido).")
    })
    public ResponseEntity<CommentResponseDTO> crearComentario(
            @Valid @RequestBody CommentCreateDTO request,
            @RequestHeader("Authorization") String token) {

        Long userIdLogueado = jwtUtil.extractUserId(token);

        CommentResponseDTO nuevoComentario = commentService.crearComentario(request, userIdLogueado, token);

        auditoriaService.registrarLog(
                userIdLogueado,
                "CREATE_COMMENT",
                "Comentario ID [" + nuevoComentario.getId() + "] publicado en el Post ID [" + request.getPostId() + "]"
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoComentario);
    }

    @GetMapping("/post/{postId}")
    @Operation(
            summary = "Obtener comentarios de una publicación",
            description = "Recupera la lista de todos los comentarios asociados a un Post específico, integrando los datos de perfil de los usuarios vía Feign."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de comentarios recuperada con éxito."),
            @ApiResponse(responseCode = "403", description = "No autorizado (Token faltante o inválido)."),
            @ApiResponse(responseCode = "404", description = "Publicación no encontrada.")
    })
    public ResponseEntity<List<CommentResponseDTO>> obtenerComentariosPorPost(
            @PathVariable Long postId,
            @RequestHeader("Authorization") String token) {

        List<CommentResponseDTO> comentarios = commentService.obtenerComentariosPorPostId(postId, token);

        return ResponseEntity.ok(comentarios);
    }

    @GetMapping("/{commentId}/author-id")
    @Operation(
            summary = "Endpoint de Comunicación Interna: ID del Autor",
            description = "Ruta de servicio de alta velocidad utilizada internamente por otros microservicios (ej: ms-Audit o ms-Interaction) para validar quién es el autor de un comentario específico."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "ID del autor devuelto exitosamente."),
            @ApiResponse(responseCode = "403", description = "No autorizado (Token faltante o inválido)."),
            @ApiResponse(responseCode = "404", description = "Autor no encontrado.")
    })
    public ResponseEntity<Long> getAuthorIdByCommentId(@PathVariable Long commentId) {
        log.info("Resolviendo autor para el Comentario ID: {}", commentId);
        Long authorId = commentService.obtenerAutorPorId(commentId);
        return ResponseEntity.ok(authorId);
    }
}
