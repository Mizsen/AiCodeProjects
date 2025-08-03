package com.example.blog.controller;

import com.example.blog.dto.PostDTO;
import com.example.blog.entity.Post;
import com.example.blog.entity.User;
import com.example.blog.repository.PostRepository;
import com.example.blog.repository.UserRepository;
import com.example.blog.util.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    private static final Logger logger = LoggerFactory.getLogger(PostController.class);
    private static final int MAX_CONTENT_SIZE = 1024 * 1024; // 1MB = 1024 * 1024 bytes

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    // 将Post实体转换为PostDTO
    private PostDTO convertToDTO(Post post) {
        PostDTO dto = new PostDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setCreateTime(post.getCreateTime());
        dto.setUpdateTime(post.getUpdateTime());
        dto.setReadCount(post.getReadCount());
        dto.setLikeCount(post.getLikeCount());
        
        // 处理作者信息
        User author = post.getAuthor();
        if (author != null) {
            dto.setAuthorId(author.getId());
            dto.setAuthorName(author.getNickname() != null ? author.getNickname() : author.getUsername());
            dto.setAuthorAvatar(author.getAvatarUrl());
        }
        
        return dto;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> createPost(@RequestBody Post post, @RequestHeader("Authorization") String authorization) {
        // 验证内容大小
        if (post.getContent() != null) {
            int contentSize = post.getContent().getBytes(StandardCharsets.UTF_8).length;
            if (contentSize > MAX_CONTENT_SIZE) {
                return ResponseEntity.badRequest().body(
                    String.format("文章内容超过限制，当前大小：%.2fMB，最大允许：1MB", 
                    contentSize / (1024.0 * 1024.0))
                );
            }
        }

        logger.info("Creating post with title: {}", post.getTitle());

        String token = authorization.substring(7);

        // 从 token 中获取用户信息
        Optional<User> userOptional = userRepository.findByToken(token);
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid token");
        }
        
        User author = userOptional.get();
        post.setAuthor(author);
        post.setCreateTime(java.time.LocalDateTime.now());
        post.setUpdateTime(java.time.LocalDateTime.now());
        
        try {
            postRepository.save(post);
            logger.info("Post created successfully with id: {}", post.getId());
            return ResponseEntity.ok("文章发布成功");
        } catch (Exception e) {
            logger.error("Error creating post", e);
            return ResponseEntity.internalServerError().body("发布文章失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getPost(@PathVariable Long id) {
        try {
            Optional<Post> postOptional = postRepository.findById(id);
            if (postOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            PostDTO dto = convertToDTO(postOptional.get());
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            logger.error("Error retrieving post with id: {}", id, e);
            return ResponseEntity.internalServerError().body("获取文章失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updatePost(@PathVariable Long id, @RequestBody Post updatedPost) {
        // 验证内容大小
        if (updatedPost.getContent() != null) {
            int contentSize = updatedPost.getContent().getBytes(StandardCharsets.UTF_8).length;
            if (contentSize > MAX_CONTENT_SIZE) {
                return ResponseEntity.badRequest().body(
                    String.format("文章内容超过限制，当前大小：%.2fMB，最大允许：1MB", 
                    contentSize / (1024.0 * 1024.0))
                );
            }
        }

        try {
            Optional<Post> postOptional = postRepository.findById(id);
            if (postOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            Post post = postOptional.get();
            post.setTitle(updatedPost.getTitle());
            post.setContent(updatedPost.getContent());
            post.setUpdateTime(java.time.LocalDateTime.now());
            postRepository.save(post);
            logger.info("Post updated successfully with id: {}", id);
            return ResponseEntity.ok("文章更新成功");
        } catch (Exception e) {
            logger.error("Error updating post with id: {}", id, e);
            return ResponseEntity.internalServerError().body("更新文章失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        try {
            if (!postRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            postRepository.deleteById(id);
            logger.info("Post deleted successfully with id: {}", id);
            return ResponseEntity.ok("文章删除成功");
        } catch (Exception e) {
            logger.error("Error deleting post with id: {}", id, e);
            return ResponseEntity.internalServerError().body("删除文章失败: " + e.getMessage());
        }
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> listPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String userId) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createTime").descending());
            Page<Post> postPage;
            
            if (userId != null && !userId.isEmpty()) {
                postPage = postRepository.findByAuthorId(Long.parseLong(userId), pageable);
            } else {
                postPage = postRepository.findAll(pageable);
            }
            
            List<PostDTO> postDTOs = postPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
                
            Map<String, Object> response = new HashMap<>();
            response.put("records", postDTOs);
            response.put("total", postPage.getTotalElements());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error listing posts", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/search")
    @Transactional(readOnly = true)
    public ResponseEntity<?> searchPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        // 添加日志记录，以便于跟踪和调试搜索请求
        logger.info("Searching posts with keyword: {}, tag: {}, page: {}, size: {}", keyword, tag, page, size);
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createTime").descending());
            Page<Post> postPage;
            String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
            String searchTag = (tag != null && !tag.trim().isEmpty()) ? tag.trim() : null;
        
            if (searchKeyword != null || searchTag != null) {
            // 使用关键字或标签搜索
            postPage = postRepository.findByKeywordAndTag(searchKeyword, searchTag, pageable);
            } else {
                // 如果没有搜索条件，返回所有文章
                postPage = postRepository.findAll(pageable);
            }
            
            List<PostDTO> postDTOs = postPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
                
            Map<String, Object> response = new HashMap<>();
            response.put("records", postDTOs);
            response.put("total", postPage.getTotalElements());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to search posts", e);
            return ResponseEntity.internalServerError().body("搜索失败：" + e.getMessage());
        }
    }
}
