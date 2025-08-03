package com.example.prescription.service.impl;

import com.example.prescription.entity.Prescription;
import com.example.prescription.entity.PrescriptionDrug;
import com.example.prescription.entity.PrescriptionImage;
import com.example.prescription.entity.PrescriptionImage;
import com.example.prescription.mapper.PrescriptionDrugMapper;
import com.example.prescription.mapper.PrescriptionImageMapper;
import com.example.prescription.mapper.PrescriptionMapper;
import com.example.prescription.service.PrescriptionService;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PrescriptionServiceImpl implements PrescriptionService {

  @Autowired
  private PrescriptionMapper prescriptionMapper;

  @Autowired
  private PrescriptionImageMapper prescriptionImageMapper;

  @Autowired
  private PrescriptionDrugMapper prescriptionDrugMapper;

  @Override
  public boolean savePrescriptionWithRelations(Prescription prescription) {
    // 保存药方、药品关联、图片等
    return prescriptionMapper.insert(prescription) > 0;
  }

  @Override
  public List<Prescription> pageQuery(
    int page,
    int size,
    String name,
    String indications
  ) {
    int offset = (page - 1) * size;
    return prescriptionMapper.pageQuery(offset, size, name, indications);
  }

  @Override
  public int count(String name, String indications) {
    return prescriptionMapper.count(name, indications);
  }

  @Override
  public Prescription getPrescriptionDetail(Integer id) {
    return prescriptionMapper.selectDetailById(id);
  }

  @Override
  public boolean updatePrescriptionWithRelations(Prescription prescription) {
    // 更新药方、药品关联、图片等
    return prescriptionMapper.updateByPrimaryKey(prescription) > 0;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean deletePrescriptionsCascade(List<Integer> ids) {
    // 级联删除药品关联、图片等，分三步，保证兼容性
    int n1 = prescriptionDrugMapper.deletePrescriptionDrugByPrescriptionIds(ids);
    int n2 = prescriptionImageMapper.deletePrescriptionImageByPrescriptionIds(ids);
    int n3 = prescriptionMapper.deletePrescriptionByIds(ids);
    return n3 > 0; // 只要主表删除成功就算成功

    // Map<String, List<Integer>> param = new HashMap<>();
    // param.put("ids1", ids);
    // param.put("ids2", ids);
    // param.put("ids3", ids);
    // return prescriptionMapper.deletePrescriptionByMap(param) > 0;
  }

  @Override
  public boolean savePrescriptionImagePath(
    Integer prescriptionId,
    String imagePath,
    String imageType
  ) {
    PrescriptionImage img = new PrescriptionImage();
    img.setPrescriptionId(prescriptionId);
    img.setImagePath(imagePath);
    img.setImageType(imageType);
    img.setSortOrder(0);
    return prescriptionImageMapper.insertPrescriptionImage(img) > 0;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public boolean savePrescriptionWithRelationsAndImages(
    Prescription prescription,
    List<Integer> drugIds,
    MultipartFile[] images
  ) {
    // 1. 保存药方
    boolean ok = prescriptionMapper.insert(prescription) > 0;
    if (!ok) return false;
    Integer prescriptionId = prescription.getId();
    try {
      // 2. 保存药品关联
      if (drugIds != null && !drugIds.isEmpty()) {
        for (Integer drugId : drugIds) {
          PrescriptionDrug pd = new PrescriptionDrug();
          pd.setPrescriptionId(prescriptionId);
          pd.setDrugId(drugId);
          pd.setDosage(""); // 可根据前端传参设置
          prescriptionDrugMapper.insert(pd);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("保存药品关联失败: " + e.getMessage(), e);
    }

    // 3. 保存图片
    if (images != null) {
      String uploadDir =
        System.getProperty("user.dir") + "/upload/prescription_image";
      File dir = new File(uploadDir);
      if (!dir.exists()) dir.mkdirs();
      for (MultipartFile file : images) {
        if (file.isEmpty()) continue;
        String originalFilename =
          org.springframework.util.StringUtils.cleanPath(
            file.getOriginalFilename()
          );
        String newFileName =
          System.currentTimeMillis() + "_" + originalFilename;
        File dest = new File(dir, newFileName);
        try {
          file.transferTo(dest);
          PrescriptionImage img = new PrescriptionImage();
          img.setPrescriptionId(prescriptionId);
          img.setImagePath("/upload/prescription_image/" + newFileName);
          String ext = "jpg";
          if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename
              .substring(originalFilename.lastIndexOf('.') + 1)
              .toLowerCase();
          }
          img.setImageType(ext);
          img.setSortOrder(0);
          prescriptionImageMapper.insertPrescriptionImage(img);
        } catch (Exception e) {
          throw new RuntimeException("图片保存失败: " + e.getMessage(), e);
        }
      }
    }
    return true;
  }

  @Override
  public List<Map<String, Object>> getDrugsByPrescriptionId(
    Integer prescriptionId
  ) {
    // 假设 prescriptionDrugMapper 有对应方法

    return prescriptionDrugMapper.selectDrugsByPrescriptionId(prescriptionId);
  }
}
