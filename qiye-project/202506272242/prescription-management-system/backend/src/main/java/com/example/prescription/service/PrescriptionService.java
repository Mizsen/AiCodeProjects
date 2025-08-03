package com.example.prescription.service;

import com.example.prescription.entity.Prescription;
import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

public interface PrescriptionService {
  boolean savePrescriptionWithRelations(Prescription prescription);
  List<Prescription> pageQuery(
    int page,
    int size,
    String name,
    String indications
  );
  int count(String name, String indications);
  Prescription getPrescriptionDetail(Integer id);
  boolean updatePrescriptionWithRelations(Prescription prescription);
  boolean deletePrescriptionsCascade(List<Integer> ids);
  boolean savePrescriptionImagePath(
    Integer prescriptionId,
    String imagePath,
    String imageType
  );
  /**
   * 保存药方、关联药品、图片，三者原子性事务
   */
  boolean savePrescriptionWithRelationsAndImages(
    Prescription prescription,
    List<Integer> drugIds,
    MultipartFile[] images
  );

  List<Map<String, Object>> getDrugsByPrescriptionId(Integer prescriptionId);
}
