package com.caseware.llm.controller;

import com.caseware.llm.service.SummaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/engagements/{companyId}/engagements")
    public ResponseEntity<Map<String, Object>> listEngagements(
            @PathVariable String companyId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        // TODO: Contract-only example. No service logic implemented yet.
        return ResponseEntity.ok(Map.of(
                "companyId", companyId,
                "engagements", java.util.List.of(
                        Map.of(
                                "engagementId", "ENG-001",
                                "templateId", "TMP-15",
                                "currentVersion", 3,
                                "latestVersion", 5,
                                "pendingUpdate", true
                        ),
                        Map.of(
                                "engagementId", "ENG-002",
                                "templateId", "TMP-20",
                                "currentVersion", 7,
                                "latestVersion", 7,
                                "pendingUpdate", false
                        )
                )
        ));
    }

    @PostMapping("/engagements/{engagementId}/update-decision")
    public ResponseEntity<Map<String, Object>> submitUpdateDecision(
            @PathVariable String engagementId,
            @RequestBody Map<String, Object> decisionPayload) {
        // TODO: Contract-only example. No service logic implemented yet.
        return ResponseEntity.accepted().body(Map.of(
                "engagementId", engagementId,
                "status", "submitted" ));
    }
}
