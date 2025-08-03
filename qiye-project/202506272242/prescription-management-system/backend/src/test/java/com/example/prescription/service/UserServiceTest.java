package com.example.prescription.service;

import com.example.prescription.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public  class UserServiceTest  {
    @Autowired
    private UserService userService;

    @Test
    void testFindUserById() {
        User user = userService.findUserById(1);
        assertNotNull(user);
    }
}