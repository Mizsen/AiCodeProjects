package com.example.blog.controller;

import com.example.blog.dto.CommentDTO;
import com.example.blog.entity.Comment;
import com.example.blog.entity.Post;
import com.example.blog.entity.User;
import com.example.blog.repository.CommentRepository;
import com.example.blog.repository.PostRepository;
import com.example.blog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

import com.example.blog.util.JsonUtils;
import org.springframework.stereotype.Controller;


@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private static final Logger logger = LogManager.getLogger(CommentController.class);

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    private CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setCreateTime(comment.getCreateTime());

        User author = comment.getAuthor();
        dto.setAuthorId(author.getId());
        dto.setAuthorName(author.getNickname() != null ? author.getNickname() : author.getUsername());
        dto.setAuthorAvatar(author.getAvatarUrl());

        Post post = comment.getPost();
        dto.setPostId(post.getId());
        dto.setPostTitle(post.getTitle());

        // 设置父评论信息
        if (comment.getParentComment() != null) {
            CommentDTO.ParentInfo parentInfo = new CommentDTO.ParentInfo();
            parentInfo.setId(comment.getParentComment().getId());
            User parentAuthor = comment.getParentComment().getAuthor();
            parentInfo.setAuthorId(parentAuthor.getId());
            parentInfo.setAuthorName(parentAuthor.getNickname() != null ? parentAuthor.getNickname() : parentAuthor.getUsername());
            parentInfo.setAuthorAvatar(parentAuthor.getAvatarUrl());
            dto.setParent(parentInfo);
        }

        // 获取回复列表
        List<CommentDTO> replies = comment.getReplies()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dto.setReplies(replies);

        return dto;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> addComment(@RequestBody Comment comment) {
        // 初始日志
        logger.info("Received comment: {}", JsonUtils.toJson(comment));

        // 检查用户是否存在
        if (comment.getAuthor() == null || comment.getAuthor().getId() == null) {
            return ResponseEntity.badRequest().body("评论作者信息不能为空");
        }
        Optional<User> userOptional = userRepository.findById(comment.getAuthor().getId());
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("用户不存在");
        }

        // 检查文章是否存在
        if (comment.getPost() == null || comment.getPost().getId() == null) {
            logger.warn("Comment post information is empty");
            return ResponseEntity.badRequest().body("评论文章信息不能为空");
        }
        Optional<Post> postOptional = postRepository.findById(comment.getPost().getId());
        if (postOptional.isEmpty()) {
            logger.warn("Post not found: {}", comment.getPost().getId());
            return ResponseEntity.badRequest().body("文章不存在");
        }

        // 获取Post和Author对象
        Post post = postOptional.get();
        User author = userOptional.get();
        logger.info("Found post with id: {}, title: {}", post.getId(), post.getTitle());
        logger.info("Found author with id: {}, username: {}", author.getId(), author.getUsername());

        // 如果是回复其他评论，检查父评论是否存在
        if (comment.getParentComment() != null && comment.getParentComment().getId() != null) {
            Optional<Comment> parentComment = commentRepository.findById(comment.getParentComment().getId());
            if (parentComment.isEmpty()) {
                return ResponseEntity.badRequest().body("父评论不存在");
            }
            comment.setParentComment(parentComment.get());
        }

        // 设置评论信息
        comment.setAuthor(author);
        comment.setPost(post);
        comment.setCreateTime(java.time.LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);
        logger.info("Comment saved successfully with id: {}", savedComment.getId());

        return ResponseEntity.ok(convertToDTO(savedComment));
    }

    @GetMapping("/post/{postId}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getCommentsByPost(@PathVariable Long postId) {

        logger.info("getCommentsByPost:{}", postId);
        try {
            // 检查文章是否存在
            if (!postRepository.existsById(postId)) {
                return ResponseEntity.badRequest().body("文章不存在");
            }

            // 只获取顶级评论（没有父评论的评论）
            List<Comment> comments = commentRepository.findByPostIdAndParentCommentIsNull(postId);
            List<CommentDTO> dtos = comments.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            logger.info("getCommentsByPost:{},{}", postId,JsonUtils.toJson(dtos));

            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            logger.error("Error getting comments for post: {}", postId, e);
            return ResponseEntity.internalServerError().body("获取评论失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable Long id) {
        if (!commentRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        commentRepository.deleteById(id);
        return ResponseEntity.ok("评论删除成功");
    }
}
