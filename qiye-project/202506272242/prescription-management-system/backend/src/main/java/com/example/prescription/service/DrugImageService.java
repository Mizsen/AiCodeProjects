package com.example.prescription.service;

import com.example.prescription.entity.DrugImage;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface DrugImageService {
    boolean saveDrugImage(DrugImage drugImage);
    List<String> saveDrugImages(Integer drugId, MultipartFile[] files, String imageType);
    List<DrugImage> getImagesByDrugId(Integer drugId);
}
