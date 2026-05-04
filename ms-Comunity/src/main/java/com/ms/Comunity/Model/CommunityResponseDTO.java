package com.ms.Comunity.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommunityResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String creatorUsername;
    private Integer memberCount;
    private LocalDateTime createdAt;
}
