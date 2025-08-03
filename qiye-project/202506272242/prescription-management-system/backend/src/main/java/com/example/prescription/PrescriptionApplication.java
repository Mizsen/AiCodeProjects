package com.example.prescription;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
public class PrescriptionApplication {

    public static void main(String[] args) {

 
        SpringApplication.run(PrescriptionApplication.class, args);
    }
}