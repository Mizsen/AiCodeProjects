package com.example.blog.controller;

import com.example.blog.entity.Like;
import com.example.blog.entity.Post;
import com.example.blog.entity.User;
import com.example.blog.repository.LikeRepository;
import com.example.blog.repository.PostRepository;
import com.example.blog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@RestController
@RequestMapping("/api/likes")
public class LikeController {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/post/{postId}/user/{userId}")
    @Transactional
    public ResponseEntity<?> addLike(@PathVariable Long postId, @PathVariable Long userId) {
        // 检查用户和文章是否存在
        Optional<User> userOptional = userRepository.findById(userId);
        Optional<Post> postOptional = postRepository.findById(postId);
        
        if (userOptional.isEmpty() || postOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("用户或文章不存在");
        }

        // 检查是否已经点赞
        if (likeRepository.existsByUserAndPost(userOptional.get(), postOptional.get())) {
            return ResponseEntity.badRequest().body("已经点赞过了");
        }

        // 创建点赞记录
        Like like = new Like();
        like.setUser(userOptional.get());
        like.setPost(postOptional.get());
        likeRepository.save(like);

        // 更新文章点赞数
        Post post = postOptional.get();
        post.setLikeCount(post.getLikeCount() + 1);
        postRepository.save(post);

        return ResponseEntity.ok("点赞成功");
    }

    @DeleteMapping("/post/{postId}/user/{userId}")
    @Transactional
    public ResponseEntity<?> removeLike(@PathVariable Long postId, @PathVariable Long userId) {
        // 检查用户和文章是否存在
        Optional<User> userOptional = userRepository.findById(userId);
        Optional<Post> postOptional = postRepository.findById(postId);
        
        if (userOptional.isEmpty() || postOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("用户或文章不存在");
        }

        // 查找点赞记录
        Optional<Like> likeOptional = likeRepository.findByUserAndPost(userOptional.get(), postOptional.get());
        if (likeOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("还没有点赞");
        }

        // 删除点赞记录
        likeRepository.delete(likeOptional.get());

        // 更新文章点赞数
        Post post = postOptional.get();
        post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
        postRepository.save(post);

        return ResponseEntity.ok("取消点赞成功");
    }

    @GetMapping("/post/{postId}/count")
    public ResponseEntity<?> getLikeCount(@PathVariable Long postId) {
        Optional<Post> postOptional = postRepository.findById(postId);
        if (postOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("文章不存在");
        }
        return ResponseEntity.ok(postOptional.get().getLikeCount());
    }
}
