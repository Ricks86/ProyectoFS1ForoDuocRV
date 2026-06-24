package com.ms.Comment.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentCreateDTO {

    @NotBlank(message = "El comentario no puede estar vacío")
    @Size(max = 1000, message = "El comentario es demasiado largo (máx 1000 caracteres)")
    private String content;

    @NotNull(message = "El Id de post es obligatorio")
    private Long postId;

}
