package com.ms.Post.Controller;


import com.ms.Post.Model.PostRequestDTO;
import com.ms.Post.Model.PostResponseDTO;
import com.ms.Post.Service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {
    private final PostService postService;

    @GetMapping
    public ResponseEntity<List<PostResponseDTO>> findAll() {
        log.info("Recibiendo solicitud para listar todos los posts");
        return ResponseEntity.ok(postService.listarPost());
    }

    @GetMapping("/comunidad/{idComunidad}")
    public ResponseEntity<List<PostResponseDTO>> findByComunidad(@PathVariable Long idComunidad){
        log.info("Recibiendo solicitud para listar posts por comunidad: {}", idComunidad);
        return ResponseEntity.ok(postService.listarPorComunidadId(idComunidad));
    }

    @PostMapping
    public ResponseEntity<PostResponseDTO> newPost(@Validated @RequestBody PostRequestDTO dto){
        log.info("Recibiendo solicitud para crear post");
        return new ResponseEntity<>(postService.crearPost(dto), HttpStatus.CREATED);
    }
}

