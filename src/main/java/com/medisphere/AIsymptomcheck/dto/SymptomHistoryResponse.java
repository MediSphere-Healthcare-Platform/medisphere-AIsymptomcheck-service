package com.medisphere.AIsymptomcheck.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SymptomHistoryResponse {

    private Integer id;
    private String patientId;
    private String age;
    private String gender;
    private List<String> symptoms;
    private String additionalInfo;
    private List<String> possibleConditions;
    private List<String> recommendedSpecialties;
    private String urgencyLevel;
    private List<String> generalAdvice;
    private LocalDateTime checkedAt;
}