package com.ms.Comment.Controller;

import com.ms.Comment.Model.CommentCreateDTO;
import com.ms.Comment.Model.CommentResponseDTO;
import com.ms.Comment.Security.JwtUtil;
import com.ms.Comment.Service.CommentService;
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
public class CommentController {

    private final CommentService commentService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<CommentResponseDTO> crearComentario(
            @Valid @RequestBody CommentCreateDTO request,
            @RequestHeader("Authorization") String token) {

        Long userIdLogueado = jwtUtil.extractUserId(token);

        CommentResponseDTO nuevoComentario = commentService.crearComentario(request, userIdLogueado);

        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoComentario);
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentResponseDTO>> obtenerComentariosPorPost(@PathVariable Long postId) {
        List<CommentResponseDTO> comentarios = commentService.obtenerComentariosPorPostId(postId);
        return ResponseEntity.ok(comentarios);
    }
}
