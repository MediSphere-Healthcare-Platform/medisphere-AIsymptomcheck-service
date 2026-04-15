package com.medisphere.AIsymptomcheck.repository;

import com.medisphere.AIsymptomcheck.domain.SymptomHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SymptomHistoryRepository
        extends JpaRepository<SymptomHistory, Integer> {

    // All checks for a specific patient
    List<SymptomHistory> findByPatientIdOrderByCheckedAtDesc(
            String patientId);

    // One specific check belonging to a patient
    Optional<SymptomHistory> findByIdAndPatientId(
            Integer id, String patientId);

    // Delete all checks for a patient
    void deleteByPatientId(String patientId);

    // Count how many checks a patient has done
    long countByPatientId(String patientId);
}