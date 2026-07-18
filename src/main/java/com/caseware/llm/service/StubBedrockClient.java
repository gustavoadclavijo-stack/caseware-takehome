package com.caseware.llm.service;

import org.springframework.stereotype.Component;

/**
 * A stubbed Bedrock client for local development. In tests this will be mocked.
 */
@Component
public class StubBedrockClient implements BedrockClient {

    @Override
    public String generateSummary(String prompt) {
        // Very naive stub that echoes the prompt with a header
        return "[LLM SUMMARY]\n" + prompt;
    }
}
