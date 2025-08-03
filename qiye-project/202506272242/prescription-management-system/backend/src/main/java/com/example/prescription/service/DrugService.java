package com.example.prescription.service;

import com.example.prescription.entity.Drug;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;


public interface DrugService {
    boolean saveDrug(Drug drug);

    boolean updateDrug(Drug drug);

    boolean deleteDrugCascade(Integer id);

    Drug getDrugDetail(Integer id);

    List<Drug> pageQueryDrug(int page, int size, String name, String indications);

    int countDrug(String name);

    boolean isDrugNameUnique(String name);


    boolean deleteDrugImage(Integer imageId);



    Map<String, Object> saveDrugWithImages(Drug drug, MultipartFile[] images,String baseUrl);
}