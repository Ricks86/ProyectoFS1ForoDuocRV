package com.ms.Comment.Model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String contenido;
    private String alias;
}
