package com.caseware.llm.controller;

import com.caseware.llm.service.SummaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class SummaryController {

    private final SummaryService summaryService;

    public SummaryController(SummaryService summaryService) {
        this.summaryService = summaryService;
    }

    @GetMapping("/engagements/{companyId}/{engagementId}/summary")
    public ResponseEntity<Map<String, String>> getSummary(@PathVariable String companyId, @PathVariable String engagementId) {
        String summary = summaryService.generateHumanReadableSummary(companyId, engagementId);
        return ResponseEntity.ok(Map.of("summary", summary));
    }
}
