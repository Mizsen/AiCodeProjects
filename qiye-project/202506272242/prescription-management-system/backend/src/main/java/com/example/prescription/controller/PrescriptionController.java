package com.example.prescription.controller;

import com.example.prescription.entity.Prescription;
import com.example.prescription.entity.PrescriptionImage;
import com.example.prescription.service.PrescriptionImageService;
import com.example.prescription.service.PrescriptionService;
import com.example.prescription.service.UserService;
import com.example.prescription.utils.JwtUtil;
import com.example.prescription.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 你的姓名
 * @since 2025-06-24
 */

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

  public static final Logger log = LoggerFactory.getLogger(
    PrescriptionController.class
  );

  @Autowired
  private PrescriptionService prescriptionService;

  @Autowired
  private JwtUtil jwtUtil;

  @Autowired
  private PrescriptionImageService prescriptionImageService;

  @Autowired
  private UserService userService;

  // 新增药方（支持 multipart/form-data）
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Map<String, Object> addPrescription(
    @RequestPart("prescription") String prescriptionStr,
    @RequestPart(value = "images", required = false) MultipartFile[] images,
    @RequestPart(value = "drugIds", required = false) String drugIdsStr,
    HttpServletRequest request
  ) {
    Map<String, Object> result = new HashMap<>();
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      Prescription prescription = objectMapper.readValue(
        prescriptionStr,
        Prescription.class
      );
      List<Integer> drugIds = new ArrayList<>();
      if (drugIdsStr != null && !drugIdsStr.isEmpty()) {
        drugIds = objectMapper.readValue(
          drugIdsStr,
          objectMapper
            .getTypeFactory()
            .constructCollectionType(List.class, Integer.class)
        );
      }
      String token = JwtUtil.resolveToken(request.getHeader("Authorization"));
      String username = JwtUtil.getUsernameFromToken(token);
      // 设置创建人
      Integer userId = userService.findByUsername(username).getId();
      prescription.setCreatedBy(userId);
      // 参数校验
      if (
        prescription.getPrescriptionName() == null ||
        prescription.getIndications() == null ||
        prescription.getUsage() == null
      ) {
        result.put("success", false);
        result.put("msg", "药方名称、适应症、用法用量为必填项");
        return result;
      }
      // 交由服务层统一处理事务
      boolean ok = prescriptionService.savePrescriptionWithRelationsAndImages(
        prescription,
        drugIds,
        images
      );
      result.put("success", ok);
      result.put("msg", ok ? "新增成功" : "新增失败");
      if (ok && prescription.getId() != null) {
        result.put("prescriptionId", prescription.getId());
      }
    } catch (Exception e) {
      result.put("success", false);
      result.put("msg", "新增失败: " + e.getMessage());
    }
    return result;
  }

  // 分页查询药方
  @GetMapping
  public Map<String, Object> listPrescriptions(
    @RequestParam(defaultValue = "1") int page,
    @RequestParam(defaultValue = "20") int size,
    @RequestParam(required = false) String name,
    @RequestParam(required = false) String indications
  ) {
    Map<String, Object> result = new HashMap<>();
    List<Prescription> list = prescriptionService.pageQuery(
      page,
      size,
      name,
      indications
    );
    int total = prescriptionService.count(name, indications);
    result.put("list", list);
    result.put("total", total);
    return result;
  }

  // 获取药方详情（含关联药品和图片）
  @GetMapping("/{id}")
  public Map<String, Object> getPrescription(@PathVariable Integer id) {
    Map<String, Object> result = new HashMap<>();
    Prescription prescription = prescriptionService.getPrescriptionDetail(id);
    // 查询关联药品
    List<Map<String, Object>> drugs =
      prescriptionService.getDrugsByPrescriptionId(id);
    // 查询药方图片
    List<PrescriptionImage> images =
      prescriptionImageService.getImagesByPrescriptionId(id);
    result.put("prescription", prescription);
    result.put("drugs", drugs);
    result.put("images", images);
    return result;
  }

  // 修改药方
  @PutMapping("/{id}")
  public Map<String, Object> updatePrescription(
    @PathVariable Integer id,
    @RequestBody Prescription prescription,
    HttpServletRequest request
  ) {
    Map<String, Object> result = new HashMap<>();
    prescription.setId(id);
    String token = JwtUtil.resolveToken(request.getHeader("Authorization"));
    String username = JwtUtil.getUsernameFromToken(token);
    String role = JwtUtil.getRoleFromToken(token);
    Prescription old = prescriptionService.getPrescriptionDetail(id);
    // 权限校验：仅创建者或管理员可修改
    // old.getCreatedBy() 需为 Integer，需通过 userService.getById(old.getCreatedBy()).getUsername() 判断
    // 这里假设 createdBy 存储的是用户名
    if (!username.equals(old.getCreatedBy()) && !"admin".equals(role)) {
      result.put("success", false);
      result.put("msg", "无权限修改");
      return result;
    }
    boolean ok = prescriptionService.updatePrescriptionWithRelations(
      prescription
    );
    result.put("success", ok);
    result.put("msg", ok ? "修改成功" : "修改失败");
    return result;
  }

  // 删除药方（支持批量）
  @DeleteMapping
  public Map<String, Object> deletePrescriptions(
    @RequestBody List<Integer> ids,
    HttpServletRequest request
  ) {
    Map<String, Object> result = new HashMap<>();
    String token = JwtUtil.resolveToken(request.getHeader("Authorization"));
    String role = JwtUtil.getRoleFromToken(token);
    if (!"admin".equals(role)) {
      result.put("success", false);
      result.put("msg", "无权限删除");
      return result;
    }
    try {
      boolean ok = prescriptionService.deletePrescriptionsCascade(ids);
      result.put("success", ok);
      result.put("msg", ok ? "删除成功" : "删除失败");
    } catch (Exception e) {
      log.error("删除药方失败", e);
      result.put("success", false);
      result.put("msg", "删除失败: " + e.getMessage());
      return result;
    }

    return result;
  }

  // 多图片上传药方图片
  @PostMapping("/{id}/images")
  public Map<String, Object> uploadPrescriptionImages(
    @PathVariable Integer id,
    @RequestParam("files") MultipartFile[] files,
    @RequestParam("imageType") String imageType
  ) {
    Map<String, Object> result = new HashMap<>();
    List<String> filePaths = new ArrayList<>();
    String uploadDir =
      System.getProperty("user.dir") + "/upload/prescription_image";
    File dir = new File(uploadDir);
    if (!dir.exists()) dir.mkdirs();

    boolean allOk = true;
    for (MultipartFile file : files) {
      if (file.isEmpty()) continue;
      String originalFilename = org.springframework.util.StringUtils.cleanPath(
        file.getOriginalFilename()
      );
      String newFileName = System.currentTimeMillis() + "_" + originalFilename;
      File dest = new File(dir, newFileName);
      try {
        file.transferTo(dest);
        com.example.prescription.entity.PrescriptionImage img =
          new com.example.prescription.entity.PrescriptionImage();
        img.setPrescriptionId(id);
        img.setImagePath("/upload/prescription_image/" + newFileName);
        img.setImageType(imageType);
        img.setSortOrder(0);
        boolean ok = prescriptionImageService.savePrescriptionImage(img);
        allOk = allOk && ok;
        filePaths.add("/upload/prescription_image/" + newFileName);
      } catch (IOException e) {
        allOk = false;
      }
    }
    result.put("success", allOk);
    result.put("paths", filePaths);
    result.put("msg", allOk ? "上传成功" : "部分或全部上传失败");
    return result;
  }
}
