package com.medisphere.AIsymptomcheck.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medisphere.AIsymptomcheck.domain.SymptomHistory;
import com.medisphere.AIsymptomcheck.dto.SymptomCheckRequest;
import com.medisphere.AIsymptomcheck.dto.SymptomCheckResponse;
import com.medisphere.AIsymptomcheck.dto.SymptomHistoryResponse;
import com.medisphere.AIsymptomcheck.exception.HistoryNotFoundException;
import com.medisphere.AIsymptomcheck.repository.SymptomHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SymptomHistoryService {

    private static final Logger log =
            LoggerFactory.getLogger(SymptomHistoryService.class);

    private final SymptomHistoryRepository historyRepository;
    private final ObjectMapper objectMapper;

    public SymptomHistoryService(
            SymptomHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
        this.objectMapper = new ObjectMapper();
    }

    // Save a symptom check result
    @Transactional
    public void saveHistory(String patientId,
                            SymptomCheckRequest request,
                            SymptomCheckResponse response) {
        try {
            SymptomHistory history = new SymptomHistory();
            history.setPatientId(patientId);
            history.setAge(request.getAge());
            history.setGender(request.getGender());
            history.setAdditionalInfo(request.getAdditionalInfo());

            // Convert lists to JSON strings for storage
            history.setSymptoms(
                    objectMapper.writeValueAsString(request.getSymptoms()));
            history.setPossibleConditions(
                    objectMapper.writeValueAsString(
                            response.getPossibleConditions()));
            history.setRecommendedSpecialties(
                    objectMapper.writeValueAsString(
                            response.getRecommendedSpecialties()));
            history.setUrgencyLevel(response.getUrgencyLevel());
            history.setGeneralAdvice(
                    objectMapper.writeValueAsString(
                            response.getGeneralAdvice()));

            historyRepository.save(history);
            log.info("Saved symptom check history for patient: {}",
                    patientId);

        } catch (Exception e) {
            // Don't fail the main response if history save fails
            log.error("Failed to save symptom history: {}",
                    e.getMessage());
        }
    }

    // Get all history for a patient
    @Transactional(readOnly = true)
    public List<SymptomHistoryResponse> getPatientHistory(
            String patientId) {
        return historyRepository
                .findByPatientIdOrderByCheckedAtDesc(patientId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get all history — admin only
    @Transactional(readOnly = true)
    public List<SymptomHistoryResponse> getAllHistory() {
        return historyRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get one specific check
    @Transactional(readOnly = true)
    public SymptomHistoryResponse getHistoryById(
            Integer id, String patientId) {
        SymptomHistory history = historyRepository
                .findByIdAndPatientId(id, patientId)
                .orElseThrow(() -> new HistoryNotFoundException(
                        "History record not found: " + id));
        return mapToResponse(history);
    }

    // Delete one specific check
    @Transactional
    public void deleteHistoryById(Integer id, String patientId) {
        SymptomHistory history = historyRepository
                .findByIdAndPatientId(id, patientId)
                .orElseThrow(() -> new HistoryNotFoundException(
                        "History record not found: " + id));
        historyRepository.delete(history);
        log.info("Deleted history record {} for patient: {}",
                id, patientId);
    }

    // Delete ALL history for a patient
    @Transactional
    public void deleteAllPatientHistory(String patientId) {
        historyRepository.deleteByPatientId(patientId);
        log.info("Deleted all history for patient: {}", patientId);
    }

    // --- Mapper ---
    private SymptomHistoryResponse mapToResponse(
            SymptomHistory history) {
        SymptomHistoryResponse res = new SymptomHistoryResponse();
        res.setId(history.getId());
        res.setPatientId(history.getPatientId());
        res.setAge(history.getAge());
        res.setGender(history.getGender());
        res.setAdditionalInfo(history.getAdditionalInfo());
        res.setUrgencyLevel(history.getUrgencyLevel());
        res.setCheckedAt(history.getCheckedAt());

        // Parse JSON strings back to lists
        res.setSymptoms(parseList(history.getSymptoms()));
        res.setPossibleConditions(
                parseList(history.getPossibleConditions()));
        res.setRecommendedSpecialties(
                parseList(history.getRecommendedSpecialties()));
        res.setGeneralAdvice(parseList(history.getGeneralAdvice()));

        return res;
    }

    private List<String> parseList(String json) {
        try {
            return objectMapper.readValue(json,
                    new TypeReference<List<String>>() {});
        } catch (Exception e) {
            // Fallback: treat as comma-separated
            return Arrays.asList(json.split(","));
        }
    }
}