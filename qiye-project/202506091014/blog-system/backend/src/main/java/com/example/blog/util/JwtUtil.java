// package com.example.blog.util;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.stereotype.Component;

// import javax.crypto.Cipher;
// import javax.crypto.spec.SecretKeySpec;
// import java.nio.charset.StandardCharsets;
// import java.security.MessageDigest;
// import java.util.Base64;

// // @Component
// public class JwtUtil {
//     @Value("${jwt.secret}")
//     private String SECRET_KEY;
//     private final long EXPIRATION = 1000 * 60 * 60 * 24; // 1天

//     // 生成token: 用户名+时间戳+密钥，先MD5摘要，再用AES加密，最后Base64编码
//     public String generateToken(String username) {
//         try {
//             long now = System.currentTimeMillis();
//             String raw = username + ":" + now + ":" + SECRET_KEY;
//             // 1. MD5摘要
//             MessageDigest md5 = MessageDigest.getInstance("MD5");
//             byte[] md5Bytes = md5.digest(raw.getBytes(StandardCharsets.UTF_8));
//             // 2. AES加密
//             SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
//             Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
//             cipher.init(Cipher.ENCRYPT_MODE, keySpec);
//             byte[] encrypted = cipher.doFinal(md5Bytes);
//             // 3. Base64编码
//             return Base64.getEncoder().encodeToString(encrypted);
//         } catch (Exception e) {
//             throw new RuntimeException(e);
//         }
//     }
// }
