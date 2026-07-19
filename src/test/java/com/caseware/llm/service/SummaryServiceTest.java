package com.caseware.llm.service;

import com.caseware.llm.model.DiffResult;
import com.caseware.llm.model.Engagement;
import com.caseware.llm.repository.EngagementRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class SummaryServiceTest {

    @Test
    void generateHumanReadableSummary_shouldReuseCachedSummaryWithoutCallingLLM() {
        EngagementRepository engagementRepository = new EngagementRepository() {
            @Override
            public Optional<Engagement> findByCompanyIdAndEngagementId(String companyId, String engagementId) {
                return Optional.of(new Engagement("company-1", "engagement-1", "template-1", "1.0", "2.0"));
            }

            @Override
            public void save(Engagement engagement) {
            }
        };

        AtomicInteger diffCalls = new AtomicInteger();
        DiffService diffService = (templateId, fromVersion, toVersion) -> {
            diffCalls.incrementAndGet();
            return new DiffResult(java.util.List.of(), java.util.List.of(), java.util.List.of());
        };

        PromptBuilder promptBuilder = new PromptBuilder();
        BedrockClient bedrockClient = prompt -> {
            throw new AssertionError("LLM should not be called when cached summary exists");
        };

        SummaryCacheService summaryCacheService = new SummaryCacheService(null, null) {
            @Override
            public Optional<String> getSummary(String key) {
                if ("summary:company-1:engagement-1:template-1:1.0:2.0".equals(key)) {
                    return Optional.of("Cached summary");
                }
                return Optional.empty();
            }
        };

        SummaryService summaryService = new SummaryService(
                engagementRepository,
                diffService,
                promptBuilder,
                bedrockClient,
                summaryCacheService
        );

        String result = summaryService.generateHumanReadableSummary("company-1", "engagement-1");

        assertThat(result).isEqualTo("Cached summary");
        assertThat(diffCalls.get()).isZero();
    }
}
