package com.ms.Comment.Model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentResponseDTO {
    private Long id;
    private String content;
    private Long postId;
    private Long userId;
    private LocalDateTime createdAt;

    private UserDTO autor;
}
