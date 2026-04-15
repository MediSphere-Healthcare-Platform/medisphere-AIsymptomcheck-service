package com.medisphere.AIsymptomcheck.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "symptom_check_history")
public class SymptomHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // patientId extracted from JWT — not a FK since separate DB
    @Column(name = "patient_id", nullable = false, length = 50)
    private String patientId;

    @Column(name = "age", length = 10)
    private String age;

    @Column(name = "gender", length = 20)
    private String gender;

    // Store symptoms list as comma-separated string
    @Column(name = "symptoms", nullable = false, columnDefinition = "text")
    private String symptoms;

    @Column(name = "additional_info", columnDefinition = "text")
    private String additionalInfo;

    // Store response lists as JSON strings
    @Column(name = "possible_conditions", columnDefinition = "text")
    private String possibleConditions;

    @Column(name = "recommended_specialties", columnDefinition = "text")
    private String recommendedSpecialties;

    @Column(name = "urgency_level", length = 10)
    private String urgencyLevel;

    @Column(name = "general_advice", columnDefinition = "text")
    private String generalAdvice;

    @Column(name = "checked_at")
    private LocalDateTime checkedAt;

    @PrePersist
    protected void onCreate() {
        checkedAt = LocalDateTime.now();
    }
}