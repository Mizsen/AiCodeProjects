package com.example.prescription.controller;

import com.example.prescription.entity.Drug;
import com.example.prescription.entity.DrugImage;
import com.example.prescription.service.DrugService;
import com.example.prescription.service.DrugImageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import java.io.File;
import java.io.IOException;
import java.util.*;

import com.example.prescription.dto.DrugDetailDTO;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 你的姓名
 * @since 2025-06-24
 */
@RestController
@RequestMapping("/api/drugs")
public class DrugController {

    private static final Logger logger = LoggerFactory.getLogger(DrugController.class);

    @Autowired
    private DrugService drugService;

    @Autowired
    private DrugImageService drugImageService;

    @Autowired
    private ObjectMapper objectMapper;



    // 修改药品
    @PutMapping("/{id}")
    public Map<String, Object> updateDrug(@PathVariable Integer id, @RequestBody Drug drug) {
        drug.setId(id);
        Map<String, Object> result = new HashMap<>();
        boolean ok = drugService.updateDrug(drug);
        result.put("success", ok);
        result.put("msg", ok ? "修改成功" : "修改失败");
        return result;
    }

    // 删除药品
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteDrug(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        boolean ok = drugService.deleteDrugCascade(id);
        result.put("success", ok);
        result.put("msg", ok ? "删除成功" : "删除失败");
        return result;
    }

    // 查询药品详情
    @GetMapping("/{id}")
    public DrugDetailDTO getDrug(@PathVariable Integer id) {
        Drug drug = drugService.getDrugDetail(id);
        List<DrugImage> images = drugImageService.getImagesByDrugId(id);
        DrugDetailDTO dto = new DrugDetailDTO();
        dto.setDrug(drug);
        dto.setImages(images);
        return dto;
    }

    // 分页查询药品
    @GetMapping
    public Map<String, Object> listDrugs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String name, @RequestParam(required = false) String indications) {
        Map<String, Object> result = new HashMap<>();
        List<Drug> list = drugService.pageQueryDrug(page, size, name,indications);
        int total = drugService.countDrug(name);
        result.put("list", list);
        result.put("total", total);
        return result;
    }

    // 药品名称唯一性校验
    @GetMapping("/check-name")
    public Map<String, Object> checkDrugName(@RequestParam String name) {
        Map<String, Object> result = new HashMap<>();
        boolean unique = drugService.isDrugNameUnique(name);
        result.put("unique", unique);
        return result;
    }

    // 上传药品图片（多张）
    @PostMapping("/{id}/images")
    public Map<String, Object> uploadDrugImages(@PathVariable Integer id,
                                                @RequestParam("files") MultipartFile[] files,
                                                @RequestParam("imageType") String imageType) {
        Map<String, Object> result = new HashMap<>();
        List<String> filePaths = drugImageService.saveDrugImages(id, files, imageType);
        boolean allOk = filePaths.size() == files.length;
        result.put("success", allOk);
        result.put("paths", filePaths);
        result.put("msg", allOk ? "上传成功" : "部分或全部上传失败");
        return result;
    }

    // 删除药品图片
    @DeleteMapping("/images/{imageId}")
    public Map<String, Object> deleteDrugImage(@PathVariable Integer imageId) {
        Map<String, Object> result = new HashMap<>();
        boolean ok = drugService.deleteDrugImage(imageId);
        result.put("success", ok);
        result.put("msg", ok ? "删除成功" : "删除失败");
        return result;
    }

    // 一步到位新增药品及图片（multipart/form-data）
    @PostMapping("/with-images")
    public Map<String, Object> addDrugWithImages(
            @RequestPart("drug") String drugJson,
            @RequestPart(value = "images", required = false) MultipartFile[] images,
            HttpServletRequest request) { // 新增参数
        logger.info("[addDrugWithImages] drugJson={}", drugJson);
        try {
            Drug drug = objectMapper.readValue(drugJson, Drug.class);
            logger.info("[addDrugWithImages] drugObj={}", drug.getDrugName());
            // String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            // 你可以把 baseUrl 传给 service 层用于拼接图片路径

            Map<String, Object> result = drugService.saveDrugWithImages(drug, images, "");
            if (result != null && Boolean.TRUE.equals(result.get("success")) && drug.getId() != null) {
                result.put("drugId", drug.getId());
            }
            return result;


        } catch (Exception e) {
            logger.error("[addDrugWithImages] 反序列化异常", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("msg", "参数解析失败: " + e.getMessage());
            return result;
        }
    }
}
