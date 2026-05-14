package com.ms.Auth.Model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponseDTO {

    private String token;
    private String username;
}
