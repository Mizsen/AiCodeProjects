package com.example.blog.repository;

import com.example.blog.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByAuthorId(Long authorId, Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE " +
           "(:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:tag IS NULL OR p.tags LIKE CONCAT('%', :tag, '%'))")
    Page<Post> findByKeywordAndTag(
        @Param("keyword") String keyword,
        @Param("tag") String tag,
        Pageable pageable
    );
}
