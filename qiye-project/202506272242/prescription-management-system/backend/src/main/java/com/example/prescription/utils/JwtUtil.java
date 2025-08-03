package com.example.prescription.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtUtil {

  // 使用安全的密钥生成方式
  // private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

  private static final String FIXED_SECRET_KEY_STRING =
    "thisisafixedsecretkeyforjsonwebtokenatleast32bytes";
  private static final SecretKey SECRET_KEY;

  private static final long EXPIRATION_TIME = 86400000; // 24小时

  static {
    // // 确保密钥长度符合要求
    // if (SECRET_KEY.getEncoded().length < 32) {
    //     throw new IllegalStateException("Secret key must be at least 256 bits long");
    // }

    // 确保密钥长度符合要求
    if (FIXED_SECRET_KEY_STRING.length() < 32) {
      throw new IllegalStateException(
        "Secret key must be at least 32 characters long"
      );
    }
    // 使用Base64解码将字符串转换成SecretKey
    SECRET_KEY = Keys.hmacShaKeyFor(
      FIXED_SECRET_KEY_STRING.getBytes(StandardCharsets.UTF_8)
    );

    // 生产环境建议不要输出密钥，仅开发调试用
    log.info(
      "JWT Secret Key initialized successfully: {}",
      Base64.getEncoder().encodeToString(SECRET_KEY.getEncoded())
    );
  }

  /**
   * 生成JWT Token
   */
  // public static String generateToken(String username) {
  //     return Jwts.builder()
  //             .setSubject(username)
  //             .setIssuedAt(new Date())
  //             .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
  //             .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
  //             .compact();
  // }

  /**
   * 生成带角色的JWT Token
   */
  public static String generateToken(String username, String role) {
    return Jwts.builder()
      .setSubject(username)
      .claim("role", role)
      .setIssuedAt(new Date())
      .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
      .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
      .compact();
  }

  /**
   * 从token获取用户名
   */
  public static String getUsernameFromToken(String token) {
    return Jwts.parserBuilder()
      .setSigningKey(SECRET_KEY)
      .build()
      .parseClaimsJws(token)
      .getBody()
      .getSubject();
  }

  /**
   * 从token获取角色
   */
  public static String getRoleFromToken(String token) {
    Claims claims = Jwts.parserBuilder()
      .setSigningKey(SECRET_KEY)
      .build()
      .parseClaimsJws(token)
      .getBody();
    Object role = claims.get("role");
    return role == null ? null : role.toString();
  }

  /**
   * 校验token有效性
   */
  public static boolean validateToken(String token) {
    try {
      Jwts.parserBuilder()
        .setSigningKey(SECRET_KEY)
        .build()
        .parseClaimsJws(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * 获取token过期时间（时间戳，毫秒）
   */
  public static long getExpireTime(String token) {
    Claims claims = Jwts.parserBuilder()
      .setSigningKey(SECRET_KEY)
      .build()
      .parseClaimsJws(token)
      .getBody();
    return claims.getExpiration().getTime();
  }

  /**
   * 从HttpServletRequest获取token
   * Java 21建议由Controller层传递header字符串，不直接依赖servlet
   */
  public static String resolveToken(String authorizationHeader) {
    if (
      authorizationHeader != null && authorizationHeader.startsWith("Bearer ")
    ) {
      return authorizationHeader.substring(7);
    }
    return null;
  }

  /**
   * 解析token，返回Claims
   */
  public static Claims parseToken(String token) {
    return Jwts.parserBuilder()
      .setSigningKey(SECRET_KEY)
      .build()
      .parseClaimsJws(token)
      .getBody();
  }
}
