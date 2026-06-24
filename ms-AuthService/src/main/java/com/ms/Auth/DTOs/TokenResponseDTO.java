package com.ms.Auth.DTOs;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponseDTO {

    private String token;
    private String username;
    private Long usuarioId;
}
