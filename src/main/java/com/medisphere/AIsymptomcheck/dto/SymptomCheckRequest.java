package com.medisphere.AIsymptomcheck.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class SymptomCheckRequest {

    @NotBlank(message = "Patient age is required")
    private String age;

    // e.g. "Male", "Female"
    @NotBlank(message = "Gender is required")
    private String gender;

    // e.g. ["headache", "fever", "sore throat"]
    @Size(min = 1, message = "At least one symptom is required")
    private List<String> symptoms;

    // optional extra context
    private String additionalInfo;
}