package com.example.blog.controller;

import com.example.blog.entity.User;
import com.example.blog.repository.UserRepository;
import com.example.blog.util.JsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;
import com.example.blog.util.CryptoUtil;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LogManager.getLogger(UserController.class);
    private static final int MAX_AVATAR_SIZE = 1024 * 1024; // 1MB
    // @Value("${jwt.secret}")
    // private String SECRET_KEY;
    private String aesKey = "1234567890abcdef";

    private final String uploadDir;

    @Autowired
    private  UserRepository userRepository;

    @Autowired
    public UserController(Environment env) {
        this.uploadDir = env.getProperty("app.upload.dir", "uploads/avatars");
    }

    // @Autowired
    // private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user, HttpServletRequest request) {
        logger.info("Register request: {}", user);
        if (userRepository.existsByUsername(user.getUsername())) {
            logger.warn("Register failed: username {} already taken", user.getUsername());
            return ResponseEntity.badRequest().body("Username is already taken");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            logger.warn("Register failed: email {} already in use", user.getEmail());
            return ResponseEntity.badRequest().body("Email is already in use");
        }
        // 密码加密存储（AES+MD5）
       // 生产环境请用更安全的密钥
        String encrypted = CryptoUtil.encryptAES(user.getPassword(), aesKey);
        String encryptedMd5 = CryptoUtil.md5(encrypted);
        user.setPassword(encryptedMd5);
        user.setRegisterTime(java.time.LocalDateTime.now());
        user.setLoginIp(request.getRemoteAddr());
        userRepository.save(user);
        logger.info("User registered successfully: {}", user.getUsername());
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User user, HttpServletRequest request) {
        logger.info("Login request: {}", user.getUsername());
        Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser.isEmpty()) {
            logger.warn("Login failed: account {} does not exist", user.getUsername());
            return ResponseEntity.badRequest().body("Account does not exist");
        }
        // 密码校验（AES+MD5）
       
        String encrypted = CryptoUtil.encryptAES(user.getPassword(), aesKey);
        String encryptedMd5 = CryptoUtil.md5(encrypted);
        if (!existingUser.get().getPassword().equals(encryptedMd5)) {
            logger.warn("Login failed: incorrect password for user {}", user.getUsername());
            return ResponseEntity.badRequest().body("Incorrect password");
        }
        // 生成 token 和过期时间
        String token = UUID.randomUUID().toString() + "-" + user.getUsername();
        LocalDateTime expiration = LocalDateTime.now().plusHours(24);
        existingUser.get().setToken(token);
        existingUser.get().setTokenExpiration(expiration);
        existingUser.get().setLastLoginTime(LocalDateTime.now());
        existingUser.get().setLoginIp(request.getRemoteAddr());
        userRepository.save(existingUser.get());
        logger.info("Login successful: {}", user.getUsername());
        return ResponseEntity.ok(Map.of("token", token, "msg", user.toString()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserInfo(@PathVariable Long id) {
        logger.info("Get user info request: id={}", id);
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            logger.info("User info found: {}", user.get().getUsername());
            return ResponseEntity.ok(user.get());
        } else {
            logger.warn("User info not found: id={}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUserInfo(@PathVariable Long id, @RequestBody User updatedUser) {
        logger.info("Update user info request: id={}, data={}", id, updatedUser);
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            logger.warn("Update failed: user id {} not found", id);
            return ResponseEntity.notFound().build();
        }
        User user = userOptional.get();
        user.setNickname(updatedUser.getNickname());
        user.setAvatarUrl(updatedUser.getAvatarUrl());
        user.setEmail(updatedUser.getEmail());
        userRepository.save(user);
        logger.info("User info updated: id={}", id);
        return ResponseEntity.ok("User info updated");
    }

    // 获取当前用户信息：校验 token 和过期时间
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUserInfo(@RequestHeader("Authorization") String authorization) {
        logger.info("Get current user info request");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            logger.warn("Authorization header is missing or invalid");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        String token = authorization.substring(7);
        Optional<User> user = userRepository.findByToken(token);
        if (user.isPresent()) {
             LocalDateTime now = LocalDateTime.now();
            if (user.get().getTokenExpiration() == null || user.get().getTokenExpiration().isBefore(now)) {
                // 如果 token 已过期，清空 token
                user.get().setToken(null);
                user.get().setTokenExpiration(null);
                userRepository.save(user.get());
                logger.warn("Token has expired for user: {}", user.get().getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token has expired. Please login again.");
            }

            logger.info("Current user info found: {}", JsonUtils.toPrettyJson(user.get()));
           
            return ResponseEntity.ok(user.get());
        } else {
            logger.warn("Current user info not found for token: {}", token);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
    }

    @PostMapping("/{id}/avatar")
    public ResponseEntity<?> uploadAvatar(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("请选择要上传的文件");
        }

        if (file.getSize() > MAX_AVATAR_SIZE) {
            return ResponseEntity.badRequest().body(
                String.format("文件大小超过限制，当前大小：%.2fMB，最大允许：1MB", 
                file.getSize() / (1024.0 * 1024.0))
            );
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body("只允许上传图片文件");
        }

        try {
            Optional<User> userOptional = userRepository.findById(id);


            if (userOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            User user = userOptional.get();

            // 创建上传目录
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成唯一的文件名
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(filename);

            // 保存文件
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            logger.info("Current user info found: {}", JsonUtils.toPrettyJson(user));

            // 删除旧头像文件
            if (user.getAvatarUrl() != null&& !user.getAvatarUrl().isEmpty()) {
                try {
                    Path oldFilePath = Paths.get(uploadDir, user.getAvatarUrl());
                    Files.deleteIfExists(oldFilePath);
                } catch (IOException e) {
                    logger.warn("Failed to delete old avatar file for user: {}", user.getUsername(), e);
                }
            }

            // 更新用户信息
            user.setAvatarUrl(filename);
            user.setAvatarContentType(contentType);
            userRepository.save(user);

            logger.info("Avatar uploaded successfully for user: {}", user.getUsername());
            return ResponseEntity.ok("头像上传成功");
        } catch (IOException e) {
            logger.error("Error uploading avatar for user id: {}", id, e);
            return ResponseEntity.internalServerError().body("头像上传失败: " + e.getMessage());
        }
    }

    @GetMapping({"/{id}/avatar", "/default/avatar"})
    public ResponseEntity<?> getAvatar(@PathVariable(required = false) String id) {
        logger.info("getAvatar params: {}", id);

        // 如果是请求默认头像
        if (id == null || id.equals("default")) {
            try {
                Path defaultAvatarPath = Paths.get(uploadDir, "default-avatar.png");
                if (!Files.exists(defaultAvatarPath)) {
                    return ResponseEntity.notFound().build();
                }
                byte[] imageBytes = Files.readAllBytes(defaultAvatarPath);
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_PNG)
                        .body(imageBytes);
            } catch (IOException e) {
                logger.error("Error reading default avatar file", e);
                return ResponseEntity.internalServerError().body("读取默认头像失败: " + e.getMessage());
            }
        }

        // 直接通过文件名读取头像
        try {
            Path filePath = Paths.get(uploadDir, id);

            logger.info("文件路径：{}", filePath);

            if (!Files.exists(filePath)) {

                logger.info("文件路径：{}", filePath);
                // 如果文件不存在，返回默认头像
                Path defaultAvatarPath = Paths.get(uploadDir, "default-avatar.png");
                if (!Files.exists(defaultAvatarPath)) {
                    return ResponseEntity.notFound().build();
                }
                byte[] imageBytes = Files.readAllBytes(defaultAvatarPath);
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_PNG)
                        .body(imageBytes);
            }

            byte[] imageBytes = Files.readAllBytes(filePath);
            // 根据文件扩展名设置Content-Type
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "image/png"; // 默认类型
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(imageBytes);
        } catch (IOException e) {
            logger.error("Error reading avatar file: {}", id, e);
            return ResponseEntity.internalServerError().body("读取头像失败: " + e.getMessage());
        }
    }
}
