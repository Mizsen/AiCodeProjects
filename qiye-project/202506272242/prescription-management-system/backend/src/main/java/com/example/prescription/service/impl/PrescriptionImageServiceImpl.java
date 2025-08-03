package com.example.prescription.service.impl;

import com.example.prescription.entity.PrescriptionImage;
import com.example.prescription.mapper.PrescriptionImageMapper;
import com.example.prescription.service.PrescriptionImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrescriptionImageServiceImpl implements PrescriptionImageService {

    @Autowired
    private PrescriptionImageMapper prescriptionImageMapper;

    @Override
    public boolean savePrescriptionImage(PrescriptionImage prescriptionImage) {
        return prescriptionImageMapper.insertPrescriptionImage(prescriptionImage) > 0;
    }

    @Override
    public List<PrescriptionImage> getImagesByPrescriptionId(Integer prescriptionId) {
        return prescriptionImageMapper.selectByPrescriptionId(prescriptionId);
    }
}
