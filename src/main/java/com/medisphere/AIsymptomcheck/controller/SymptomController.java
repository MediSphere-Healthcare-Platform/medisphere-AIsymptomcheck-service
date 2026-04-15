package com.medisphere.AIsymptomcheck.controller;

import com.medisphere.AIsymptomcheck.dto.SymptomCheckRequest;
import com.medisphere.AIsymptomcheck.dto.SymptomCheckResponse;
import com.medisphere.AIsymptomcheck.dto.SymptomHistoryResponse;
import com.medisphere.AIsymptomcheck.service.SymptomService;
import com.medisphere.AIsymptomcheck.service.SymptomHistoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/symptoms")
public class SymptomController {

    private final SymptomService geminiService;
    private final SymptomHistoryService historyService;

    public SymptomController(SymptomService SymptomService,
                             SymptomHistoryService historyService) {
        this.geminiService = SymptomService;
        this.historyService = historyService;
    }

    // Check symptoms — auto saves to history
    @PostMapping("/check")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    public ResponseEntity<SymptomCheckResponse> checkSymptoms(
            @Valid @RequestBody SymptomCheckRequest request) {

        String patientId = getCurrentUserId();
        SymptomCheckResponse response =
                geminiService.checkSymptoms(request);

        // Save to history — non-blocking
        historyService.saveHistory(patientId, request, response);

        return ResponseEntity.ok(response);
    }

    // Get logged-in patient's history
    @GetMapping("/history")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<SymptomHistoryResponse>> getMyHistory() {
        String patientId = getCurrentUserId();
        return ResponseEntity.ok(
                historyService.getPatientHistory(patientId));
    }

    // Get one specific history record
    @GetMapping("/history/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    public ResponseEntity<SymptomHistoryResponse> getHistoryById(
            @PathVariable Integer id) {
        String patientId = getCurrentUserId();
        return ResponseEntity.ok(
                historyService.getHistoryById(id, patientId));
    }

    // Admin — get ALL patients' history
    @GetMapping("/history/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SymptomHistoryResponse>> getAllHistory() {
        return ResponseEntity.ok(historyService.getAllHistory());
    }

    // Delete one specific record
    @DeleteMapping("/history/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    public ResponseEntity<Map<String, String>> deleteHistoryById(
            @PathVariable Integer id) {
        String patientId = getCurrentUserId();
        historyService.deleteHistoryById(id, patientId);
        return ResponseEntity.ok(
                Map.of("message", "History record deleted successfully"));
    }

    // Delete ALL history for logged-in patient
    @DeleteMapping("/history")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Map<String, String>> deleteAllMyHistory() {
        String patientId = getCurrentUserId();
        historyService.deleteAllPatientHistory(patientId);
        return ResponseEntity.ok(
                Map.of("message", "All history deleted successfully"));
    }

    // Health check
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "service", "symptom-checker-service",
                "status", "UP",
                "aiProvider", "Groq - Llama 3.3 70B"
        ));
    }

    // Helper — get userId from JWT
    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder
                .getContext().getAuthentication();
        return auth.getPrincipal().toString();
    }
}