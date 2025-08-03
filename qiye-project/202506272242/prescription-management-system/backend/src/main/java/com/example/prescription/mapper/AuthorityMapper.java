package com.example.prescription.mapper;

import com.example.prescription.entity.Authority;
import java.util.List;

public interface AuthorityMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Authority row);

    Authority selectByPrimaryKey(Integer id);

    List<Authority> selectAll();

    int updateByPrimaryKey(Authority row);
}