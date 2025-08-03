package com.example.prescription.dto;

import com.example.prescription.entity.Drug;
import com.example.prescription.entity.DrugImage;
import java.util.List;

public class DrugDetailDTO {
    private Drug drug;
    private List<DrugImage> images;

    public Drug getDrug() { return drug; }
    public void setDrug(Drug drug) { this.drug = drug; }

    public List<DrugImage> getImages() { return images; }
    public void setImages(List<DrugImage> images) { this.images = images; }
}