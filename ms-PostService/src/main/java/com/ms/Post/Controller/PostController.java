package com.ms.Post.Controller;


import com.ms.Post.Model.PostCreateDTO;
import com.ms.Post.Model.PostFeedDTO;
import com.ms.Post.Model.PostResponseDTO;
import com.ms.Post.Security.JwtUtil;
import com.ms.Post.Service.AuditService;
import com.ms.Post.Service.PostService;
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

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final JwtUtil jwtUtil;
    private final AuditService auditoriaService;

    @PostMapping
    public ResponseEntity<PostResponseDTO> crearPost(
            @Valid @RequestBody PostCreateDTO request,
            @RequestHeader("Authorization") String token) {

        Long idUsuarioLogueado = jwtUtil.extractUserId(token);

        PostResponseDTO nuevoPost = postService.crearPost(request, idUsuarioLogueado);

        auditoriaService.registrarLog(
                idUsuarioLogueado,
                "CREATE_POST",
                "Post publicado exitosamente con ID [" + nuevoPost.getId() + "] y título: '" + nuevoPost.getTitulo()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoPost);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(postService.obtenerPostPorId(id));
    }

    @GetMapping("/filtrar/antes-de")
    public ResponseEntity<List<PostResponseDTO>> obtenerAntesDe(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        LocalDateTime fechaLimite = fecha.atStartOfDay();

        return ResponseEntity.ok(postService.obtenerPostsCreadosAntesDe(fechaLimite));
    }

    @GetMapping("/usuario/{username}")
    public ResponseEntity<List<PostResponseDTO>> obtenerPorUsername(@PathVariable String username) {
        return ResponseEntity.ok(postService.obtenerPostsPorUsername(username));
    }

    @GetMapping("/feed")
    public ResponseEntity<Page<PostFeedDTO>> obtenerFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<PostFeedDTO> feed = postService.getFeedPaginado(pageable);
        return ResponseEntity.ok(feed);
    }
}
