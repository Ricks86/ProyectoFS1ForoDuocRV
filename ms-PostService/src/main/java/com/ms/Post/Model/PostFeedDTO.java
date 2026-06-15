package com.ms.Post.Model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor

@Schema(title = "DTO para el feed")

public class PostFeedDTO {
    private Long id;
    private String titulo;
    private String contenido;
    private LocalDateTime createdAt;

    private UserDTO autor;
}
