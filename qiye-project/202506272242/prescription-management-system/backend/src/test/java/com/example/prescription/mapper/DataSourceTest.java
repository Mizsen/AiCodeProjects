package com.example.prescription.mapper;

import com.example.prescription.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest
public class DataSourceTest {
    @Autowired
    private UserMapper userMapper;


    @Test
    @Transactional(transactionManager = "transactionManager")
    public void testSlaveDataSource() {
        User user = userMapper.selectByPrimaryKey(1);
        assertNotNull(user);
    }
}