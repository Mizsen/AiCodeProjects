package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "jobs")
@Data
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String company;
    private String location;
    private String salary;
    private String experience;
    private String education;
    private String description;
    private String url;
    @Column(name = "query_type")
    private String queryType;
    private String city;
    @Column(name = "created_at")
    private String createdAt;
}
