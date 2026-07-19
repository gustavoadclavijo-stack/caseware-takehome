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
    private final SummaryCacheService summaryCacheService;

    public SummaryService(EngagementRepository engagementRepository,
                          DiffService diffService,
                          PromptBuilder promptBuilder,
                          BedrockClient bedrockClient,
                          SummaryCacheService summaryCacheService) {
        this.engagementRepository = engagementRepository;
        this.diffService = diffService;
        this.promptBuilder = promptBuilder;
        this.bedrockClient = bedrockClient;
        this.summaryCacheService = summaryCacheService;
    }

    public String generateHumanReadableSummary(String companyId, String engagementId) {
        Optional<Engagement> optionalEngagement = engagementRepository.findByCompanyIdAndEngagementId(companyId, engagementId);
        if (optionalEngagement.isEmpty()) {
            throw new IllegalArgumentException("Engagement not found");
        }
        Engagement currentEngagement = optionalEngagement.get();

        if (currentEngagement.getCurrentTemplateVersion().equals(currentEngagement.getLatestTemplateVersion())) {
            return "No update pending";
        }

        String cacheKey = buildCacheKey(companyId, engagementId, currentEngagement.getTemplateId(), currentEngagement.getCurrentTemplateVersion(), currentEngagement.getLatestTemplateVersion());
        Optional<String> cachedSummary = summaryCacheService.getSummary(cacheKey);
        if (cachedSummary.isPresent()) {
            return cachedSummary.get();
        }

        return summaryCacheService.executeWithLock("lock:" + cacheKey, () -> {
            Optional<String> summaryFromCache = summaryCacheService.getSummary(cacheKey);
            if (summaryFromCache.isPresent()) {
                return summaryFromCache.get();
            }
            DiffResult diff = diffService.computeDiff(currentEngagement.getTemplateId(), currentEngagement.getCurrentTemplateVersion(), currentEngagement.getLatestTemplateVersion());
            String prompt = promptBuilder.buildPrompt(currentEngagement.getTemplateId(), currentEngagement.getCurrentTemplateVersion(), currentEngagement.getLatestTemplateVersion(), diff);
            String summary = bedrockClient.generateSummary(prompt);
            summaryCacheService.saveSummary(cacheKey, summary);
            return summary;
        });
    }

    private String buildCacheKey(String companyId, String engagementId, String templateId, String currentVersion, String latestVersion) {
        return String.format("summary:%s:%s:%s:%s:%s", companyId, engagementId, templateId, currentVersion, latestVersion);
    }
}
