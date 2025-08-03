package com.example.prescription.mapper;

import com.example.prescription.entity.PrescriptionImage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

public interface PrescriptionImageMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(PrescriptionImage row);

    PrescriptionImage selectByPrimaryKey(Integer id);

    List<PrescriptionImage> selectAll();

    int updateByPrimaryKey(PrescriptionImage row);

    int insertPrescriptionImage(PrescriptionImage prescriptionImage);

    List<PrescriptionImage> selectByPrescriptionId(Integer prescriptionId);

    /**
     * 批量删除药方关联的图片
     */
    int deletePrescriptionImageByPrescriptionIds(List<Integer> ids);
}