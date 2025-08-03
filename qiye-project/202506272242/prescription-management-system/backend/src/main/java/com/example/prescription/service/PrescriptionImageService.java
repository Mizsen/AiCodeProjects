package com.example.prescription.service;

import com.example.prescription.entity.PrescriptionImage;

import java.util.List;

public interface PrescriptionImageService {
    boolean savePrescriptionImage(PrescriptionImage prescriptionImage);

    List<PrescriptionImage> getImagesByPrescriptionId(Integer prescriptionId);
}
