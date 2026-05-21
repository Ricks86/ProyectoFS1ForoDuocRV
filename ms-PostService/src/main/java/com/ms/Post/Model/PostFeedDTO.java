package com.ms.Post.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostFeedDTO {
    private Long id;
    private String titulo;
    private String contenido;
    private LocalDateTime createdAt;

    private UserDTO autor;
}
