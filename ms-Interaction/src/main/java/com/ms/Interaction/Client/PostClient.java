package com.ms.Interaction.Client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "ms-PostService", url = "http://localhost:8083/api/posts")
public interface PostClient {
    @GetMapping("/{postId}/author-id")
    Long getAuthorIdByPostId(@PathVariable Long postId, @RequestHeader("Authorization") String token);
}