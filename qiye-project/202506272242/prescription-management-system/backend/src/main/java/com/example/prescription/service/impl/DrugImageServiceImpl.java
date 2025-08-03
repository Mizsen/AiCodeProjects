package com.example.prescription.service.impl;

import com.example.prescription.entity.DrugImage;
import com.example.prescription.mapper.DrugImageMapper;
import com.example.prescription.service.DrugImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DrugImageServiceImpl implements DrugImageService {

    @Autowired
    private DrugImageMapper drugImageMapper;

    @Override
    public boolean saveDrugImage(DrugImage drugImage) {
        return drugImageMapper.insertDrugImage(drugImage) > 0;
    }

    @Override
    public List<String> saveDrugImages(Integer drugId, MultipartFile[] files, String imageType) {
        List<String> filePaths = new ArrayList<>();
        // String uploadDir = System.getProperty("user.dir") + "/upload/drug";
        // File dir = new File(uploadDir);
        // if (!dir.exists()) dir.mkdirs();

        // for (MultipartFile file : files) {
        //     if (file.isEmpty()) continue;
        //     String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        //     String newFileName = System.currentTimeMillis() + "_" + originalFilename;
        //     File dest = new File(dir, newFileName);
        //     try {
        //         file.transferTo(dest);
        //         DrugImage img = new DrugImage();
        //         img.setDrugId(drugId);
        //         String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        //         String imageUrl = baseUrl + "/upload/" + newFileName;

        //         // img.setImagePath("/upload/drug/" + newFileName);
        //         img.setImagePath(imageUrl);
        //         img.setImageType(imageType);
        //         img.setSortOrder(0);
        //         if (this.saveDrugImage(img)) {
        //             // filePaths.add("/upload/drug/" + newFileName);
        //             filePaths.add(imageUrl);
        //         }
        //     } catch (IOException e) {
        //         // ignore this file, do not add to filePaths
        //     }
        // }
        return filePaths;
    }

    @Override
    public List<DrugImage> getImagesByDrugId(Integer drugId) {
        return drugImageMapper.selectImagesByDrugId(drugId);
    }
}
