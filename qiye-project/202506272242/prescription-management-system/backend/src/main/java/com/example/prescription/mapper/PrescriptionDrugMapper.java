package com.example.prescription.mapper;

import com.example.prescription.entity.PrescriptionDrug;
import java.util.List;

public interface PrescriptionDrugMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(PrescriptionDrug row);

    PrescriptionDrug selectByPrimaryKey(Integer id);

    List<PrescriptionDrug> selectAll();

    int updateByPrimaryKey(PrescriptionDrug row);

    /**
     * 根据药方ID查询关联药品（返回药品信息列表）
     */
    List<java.util.Map<String, Object>> selectDrugsByPrescriptionId(Integer prescriptionId);

    /**
     * 批量删除药方关联的药品
     */
    int deletePrescriptionDrugByPrescriptionIds(List<Integer> ids);

}