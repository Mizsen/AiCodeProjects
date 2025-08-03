package com.example.prescription.mapper;

import com.example.prescription.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User row);

    User selectByPrimaryKey(Integer id);

    List<User> selectAll();

    int updateByPrimaryKey(User row);

    User selectByUsername(String username);
}