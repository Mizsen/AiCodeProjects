package com.example.backend.controller;

import com.example.backend.entity.User;
import com.example.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.backend.auth.RequireLogin;
import com.example.backend.auth.TokenManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")

public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private TokenManager tokenManager;

    @RequireLogin
    @GetMapping
    public List<User> list() {
        return userService.findAll();
    }

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return userService.register(user);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User user) {
        User loginUser = userService.login(user.getUsername(), user.getPassword());
        if (loginUser != null) {
            String token = tokenManager.generateToken(loginUser.getUsername());
            Map<String, Object> result = new HashMap<>();
            result.put("user", loginUser);
            result.put("token", token);
            return result;
        } else {
            throw new RuntimeException("用户名或密码错误");
        }
    }

    @RequireLogin
    @PutMapping("/{id}")
    public User update(@PathVariable Long id, @RequestBody User user) {
        User dbUser = userService.findById(id);
        if (dbUser != null) {
            dbUser.setEmail(user.getEmail());
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                dbUser.setPassword(user.getPassword());
            }
            return userService.save(dbUser);
        }
        return null;
    }

    @RequireLogin
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userService.deleteById(id);
    }
}
