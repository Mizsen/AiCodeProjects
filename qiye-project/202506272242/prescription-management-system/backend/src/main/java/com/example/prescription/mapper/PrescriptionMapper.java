package com.example.prescription.mapper;

import com.example.prescription.entity.Prescription;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

public interface PrescriptionMapper {
  int deleteByPrimaryKey(Integer id);

  int insert(Prescription row);

  Prescription selectByPrimaryKey(Integer id);

  List<Prescription> selectAll();

  int updateByPrimaryKey(Prescription prescription);

  List<Prescription> pageQuery(
    int offset,
    int size,
    String name,
    String indications
  );

  int count(String name, String indications);

  Prescription selectDetailById(Integer id);


  /**
   * 批量删除药方
   * @param ids 药方ID列表
   * @return 删除的记录数
   */
  int deletePrescriptionByIds(List<Integer> ids);

  /**
   *
   */
  int deletePrescriptionByMap(Map<String, List<Integer>> paramMap);

  int deletePrescriptionByMoreList( @Param("drugIds") List<Integer> drugIds,
    @Param("imageIds") List<Integer> imageIds,
    @Param("prescriptionIds") List<Integer> prescriptionIds);
}
