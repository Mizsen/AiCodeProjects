package com.example.prescription.dto;

import lombok.Data;

@Data
public class UserRegisterDTO {
    private String username;
    private String password;
    private String realName;
    private String role; // "ADMIN", "DOCTOR", "PHARMACIST"
}