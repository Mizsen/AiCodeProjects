package com.example.blog.config;

import com.example.blog.entity.User;
import com.example.blog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
//            response.sendRedirect("/");
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"redirect\":\"/login\"}");
            return false;

        }
        token = token.substring(7);
        Optional<User> userOpt = userRepository.findByToken(token);
        if (userOpt.isEmpty() || userOpt.get().getTokenExpiration() == null || userOpt.get().getTokenExpiration().isBefore(LocalDateTime.now())) {
//            response.sendRedirect("/");
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"redirect\":\"/login\"}");
            return false;

        }
        // 更新最后登录时间和IP
        User user = userOpt.get();
        user.setLastLoginTime(LocalDateTime.now());
        user.setLoginIp(request.getRemoteAddr());
        userRepository.save(user);
        return true;
    }
}
