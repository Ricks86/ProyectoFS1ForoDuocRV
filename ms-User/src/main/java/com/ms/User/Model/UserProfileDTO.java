package com.ms.User.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDTO {

    private String username;
    private String bio;
    private String avatarUrl;
    private Integer reputationLevel;
    private Integer followersCount;
    private Integer followingCount;
}
