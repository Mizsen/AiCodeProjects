package com.example.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class JobStatisticsDTO {
    private List<String> months;
    private List<Integer> counts;
    private int total;
}
