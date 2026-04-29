package com.ms.Comunity.Model;

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
