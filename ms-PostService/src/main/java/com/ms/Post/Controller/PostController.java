package com.ms.Post.Controller;


import com.ms.Post.Model.PostCreateDTO;
import com.ms.Post.Model.PostResponseDTO;
import com.ms.Post.Security.JwtUtil;
import com.ms.Post.Service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<PostResponseDTO> crearPost(
            @Valid @RequestBody PostCreateDTO request,
            @RequestHeader("Authorization") String token) {

        Long idUsuarioLogueado = jwtUtil.extractUserId(token);

        PostResponseDTO nuevoPost = postService.crearPost(request, idUsuarioLogueado);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoPost);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(postService.obtenerPostPorId(id));
    }

    @GetMapping("/filtrar/antes-de")
    public ResponseEntity<List<PostResponseDTO>> obtenerAntesDe(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fecha) {
        return ResponseEntity.ok(postService.obtenerPostsCreadosAntesDe(fecha));
    }

    @GetMapping("/usuario/{username}")
    public ResponseEntity<List<PostResponseDTO>> obtenerPorUsername(@PathVariable String username) {
        return ResponseEntity.ok(postService.obtenerPostsPorUsername(username));
    }
}
