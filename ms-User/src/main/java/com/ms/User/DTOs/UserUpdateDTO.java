package com.ms.User.DTOs;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateDTO {
    @Size(max = 500, message = "La biografia no puede superar los 500 caracteres")
    private String bio;

    private String avatarUrl;


}
