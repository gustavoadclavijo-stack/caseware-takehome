package com.caseware.llm.service;

/**
 * Abstraction over the LLM provider (e.g., Amazon Bedrock). Implementations can call AWS SDK.
 * For this exercise the implementation will be mocked in tests or use the stub.
 */
public interface BedrockClient {
    String generateSummary(String prompt);
}
