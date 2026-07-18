package com.caseware.llm.service;

import com.caseware.llm.model.DiffResult;
import com.caseware.llm.model.Engagement;
import com.caseware.llm.repository.EngagementRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SummaryService {

    private final EngagementRepository engagementRepository;
    private final DiffService diffService;
    private final PromptBuilder promptBuilder;
    private final BedrockClient bedrockClient;

    public SummaryService(EngagementRepository engagementRepository,
                          DiffService diffService,
                          PromptBuilder promptBuilder,
                          BedrockClient bedrockClient) {
        this.engagementRepository = engagementRepository;
        this.diffService = diffService;
        this.promptBuilder = promptBuilder;
        this.bedrockClient = bedrockClient;
    }

    public String generateHumanReadableSummary(String companyId, String engagementId) {
        Optional<Engagement> opt = engagementRepository.findByCompanyIdAndEngagementId(companyId, engagementId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Engagement not found");
        }
        Engagement e = opt.get();

        // If versions are equal, nothing to do
        if (e.getCurrentTemplateVersion().equals(e.getLatestTemplateVersion())) {
            return "No update pending";
        }

        // Compute diff between current and latest
        DiffResult diff = diffService.computeDiff(e.getTemplateId(), e.getCurrentTemplateVersion(), e.getLatestTemplateVersion());

        // Build prompt
        String prompt = promptBuilder.buildPrompt(e.getTemplateId(), e.getCurrentTemplateVersion(), e.getLatestTemplateVersion(), diff);

        // Call LLM (Bedrock)
        String summary = bedrockClient.generateSummary(prompt);

        return summary;
    }
}
