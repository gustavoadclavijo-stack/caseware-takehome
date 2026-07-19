package com.caseware.llm.service;

import com.caseware.llm.model.DiffResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PromptBuilderTest {

    @Test
    void buildPrompt_shouldUseStructuredTemplateWithResolvedPlaceholders() {
        PromptBuilder promptBuilder = new PromptBuilder();
        DiffResult diff = new DiffResult(
                List.of("Add approval field"),
                List.of("Update signature block"),
                List.of("Remove obsolete section")
        );

        String prompt = promptBuilder.buildPrompt("template-42", "1.0", "2.0", diff);

        assertThat(prompt).contains("<context>");
        assertThat(prompt).contains("<changes>");
        assertThat(prompt).contains("<instructions>");
        assertThat(prompt).contains("<output>");
        assertThat(prompt).contains("Template ID: template-42");
        assertThat(prompt).contains("From Version: 1.0");
        assertThat(prompt).contains("To Version: 2.0");
        assertThat(prompt).contains("- Add approval field");
        assertThat(prompt).contains("Summary:");
    }
}
