package com.ms.Interaction.Client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "ms-CommentService", url = "http://localhost:8084/api/comments")
public interface CommentClient {
    @GetMapping("/{commentId}/author-id")
    Long getAuthorIdByCommentId(@PathVariable("commentId") Long commentId, @RequestHeader("Authorization") String token);
}
