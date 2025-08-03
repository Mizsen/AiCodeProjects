package com.example.prescription.mapper;

import com.example.prescription.entity.DrugImage;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;


public interface DrugImageMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(DrugImage row);

    DrugImage selectByPrimaryKey(Integer id);

    List<DrugImage> selectAll();

    int updateByPrimaryKey(DrugImage row);

    int insertDrugImage(DrugImage drugImage);

    int deleteByDrugId(Integer drugId);

    int deleteDrugImageById(Integer id);

    List<DrugImage> selectImagesByDrugId(Integer drugId);
}