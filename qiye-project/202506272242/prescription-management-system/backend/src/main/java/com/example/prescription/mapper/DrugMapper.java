package com.example.prescription.mapper;

import com.example.prescription.entity.Drug;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface DrugMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Drug row);

    Drug selectByPrimaryKey(Integer id);

    List<Drug> selectAll();

    int updateByPrimaryKey(Drug row);

    int insertDrug(Drug drug);

    int updateDrugById(Drug drug);

    int deleteDrugById(Integer id);

    Drug selectDrugDetailById(Integer id);

    List<Drug> pageQueryDrug(int offset, int size, String name,String indications);

    int countDrug(String name);

    int countByDrugName(String name);
}