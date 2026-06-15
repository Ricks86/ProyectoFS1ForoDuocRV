package com.ms.Post.Model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Schema(title = "DTO para acceder a datos en la tabla Users")

public class UserDTO {
    private Long id;
    private String username;
    private String alias;
}