package com.ms.Post.Repository;

import com.ms.Post.Model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByFechaCreacionBefore(LocalDateTime fecha);

    List<Post> findByIdUsuario(Long idUsuario);

    List<Post> findByIdUsuarioAndFechaCreacionBefore(Long idUsuario, LocalDateTime fecha);
}