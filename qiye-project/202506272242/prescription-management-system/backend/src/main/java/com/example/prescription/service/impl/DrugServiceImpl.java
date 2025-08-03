package com.example.prescription.service.impl;

import com.example.prescription.entity.Drug;
import com.example.prescription.entity.DrugImage;
import com.example.prescription.mapper.DrugMapper;
import com.example.prescription.mapper.DrugImageMapper;
import com.example.prescription.service.DrugService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DrugServiceImpl implements DrugService {

    private static final Logger logger = LoggerFactory.getLogger(DrugServiceImpl.class);

    @Autowired
    private DrugMapper drugMapper;
    @Autowired
    private DrugImageMapper drugImageMapper;

    @Override
    public boolean saveDrug(Drug drug) {
        return drugMapper.insertDrug(drug) > 0;
    }

    @Override
    public boolean updateDrug(Drug drug) {
        return drugMapper.updateDrugById(drug) > 0;
    }

    @Override
    public boolean deleteDrugCascade(Integer id) {
        drugImageMapper.deleteByDrugId(id);
        // 还可删除drug与prescription的关联
        return drugMapper.deleteDrugById(id) > 0;
    }

    @Override
    public Drug getDrugDetail(Integer id) {
        return drugMapper.selectDrugDetailById(id);
    }

    @Override
    public List<Drug> pageQueryDrug(int page, int size, String name,String indications) {
        int offset = (page - 1) * size;
        return drugMapper.pageQueryDrug(offset, size, name, indications);
    }

    @Override
    public int countDrug(String name) {
        return drugMapper.countDrug(name);
    }

    @Override
    public boolean isDrugNameUnique(String name) {
        return drugMapper.countByDrugName(name) == 0;
    }


    @Override
    public boolean deleteDrugImage(Integer imageId) {
        return drugImageMapper.deleteDrugImageById(imageId) > 0;
    }

   
   

    @Override
    @Transactional
    public Map<String, Object> saveDrugWithImages(Drug drug, MultipartFile[] images,String baseUrl) {
        Map<String, Object> result = new HashMap<>();
        boolean drugOk = drugMapper.insertDrug(drug) > 0;
        logger.info("[saveDrugWithImages] 保存药品：{}，结果：{}", drug, drugOk);
        if (!drugOk) {
            result.put("success", false);
            result.put("msg", "药品保存失败");
            return result;
        }
        logger.info("[saveDrugWithImages] 药品保存成功，ID：{}", drug.getId());
        logger.info("[saveDrugWithImages] 开始保存药品图片，数量：{}", images != null ? images.length : 0);

        boolean allImgOk = true;
        if (images != null && images.length > 0) {
            String uploadDir = System.getProperty("user.dir") + File.separator + "upload" + File.separator + "drug_image";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();
            for (MultipartFile file : images) {
                try {
                    String fileName = System.currentTimeMillis() + "_" + (int)(Math.random()*10000) + ".jpg";
                    Path filePath = Paths.get(uploadDir, fileName);
                    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                    logger.info("[saveDrugWithImages] 图片已保存到：{}，原文件名：{}", filePath, file.getOriginalFilename());
                    DrugImage img = new DrugImage();
                    img.setDrugId(drug.getId());
                    // img.setImagePath("/upload/drug_image/" + fileName);
                    String imageUrl = baseUrl + "/upload/drug_image/" + fileName;
                    img.setImagePath(imageUrl);
                    // 获取图片类型（后缀），如 jpg/png/gif
                    String originalFilename = file.getOriginalFilename();
                    String ext = "jpg";
                    if (originalFilename != null && originalFilename.contains(".")) {
                        ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
                    }
                    img.setImageType(ext);
                    img.setSortOrder(0);
                    int n = drugImageMapper.insertDrugImage(img);
                    logger.info("[saveDrugWithImages] 图片数据库写入：{}，结果：{}", img, n > 0);
                    if (n <= 0) allImgOk = false;
                } catch (Exception e) {
                    logger.error("[saveDrugWithImages] 图片保存异常", e);
                    allImgOk = false;
                }
            }
        }
        result.put("success", allImgOk);
        result.put("msg", allImgOk ? "保存成功" : "部分图片保存失败");
        result.put("drugId", drug.getId());
        return result;
    }
}

