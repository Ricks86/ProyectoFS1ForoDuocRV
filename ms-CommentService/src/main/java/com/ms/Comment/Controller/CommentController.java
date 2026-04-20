package com.ms.Comment.Controller;

import com.ms.Comment.Model.CommentRequestDTO;
import com.ms.Comment.Model.CommentResponseDTO;
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

    @PostMapping
    public ResponseEntity<CommentResponseDTO> crear(@Valid @RequestBody CommentRequestDTO dto) {
        log.info("Petición para crear comentario para el post {}", dto.getPostId());
        CommentResponseDTO response = commentService.crearComentario(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentResponseDTO>> obtenerComentarioPorPost(@PathVariable Long postId) {
        log.info("Petición para listar comentarios del post {}", postId);
        List<CommentResponseDTO> comentarios = commentService.getComentarioByPostId(postId);
        return ResponseEntity.ok(comentarios);
    }
}
