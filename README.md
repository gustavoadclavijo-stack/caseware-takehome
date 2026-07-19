# Caseware - LLM Summary Generator

This small Spring Boot service implements a focused part of the architecture: given an engagement record (which contains template id and versions), compute the JSON diff between the engagement's current template version and the latest template version, build a human-friendly LLM prompt, invoke an LLM (abstracted as BedrockClient), and return a human-readable summary.

This project is intentionally minimal and focuses on the requested flow only. The build uses Gradle.

What is implemented

- In-memory Engagement repository simulating a DynamoDB materialized view (InMemoryEngagementRepository).
- A DiffService interface (with a local stub) that represents the external template-diff API; in unit tests this is mocked.
- PromptBuilder: converts a DiffResult into a clear, structured prompt for an LLM using a template file stored in resources and placeholder-based substitution.
- BedrockClient interface (with a local stub) that represents Amazon Bedrock; in unit tests this is mocked.
- SummaryService: orchestrates the flow: load engagement -> compute diff -> build prompt -> call LLM -> return summary.
- SummaryController: simple REST endpoint: GET /api/v1/engagements/{companyId}/{engagementId}/summary
- Unit tests using JUnit + Mockito that mock DiffService and BedrockClient.

How to run

- Build with the Gradle wrapper (preferred):
  - ./gradlew clean build
  - ./gradlew bootRun

- If you want to run the Docker-based integration test as well, use:
  - RUN_DOCKER_TESTS=true ./gradlew test

- Using the system Gradle installation is not recommended in this environment because it can pick up a different JVM/Gradle combination and fail unexpectedly.

- Example endpoints (after start):
  - GET http://localhost:8080/api/v1/engagements/companyA/eng-1/summary -> returns generated summary (sample seeded data)
  - GET http://localhost:8080/api/v1/engagements/companyA/eng-2/summary -> "No update pending"

Design notes

- EngagementRepository is intentionally an in-memory store (ConcurrentHashMap) to simulate the DynamoDB materialized view. In real production code this would be replaced by a DynamoDB-backed repository.

- DiffService is an abstraction over the existing template-diff API you mentioned. The service returns a DiffResult object with lists of added/modified/removed items. In unit tests we mock this component.

- PromptBuilder now uses a prompt template externalized in resources with sections such as context, changes, instructions, and output. It replaces placeholders with the supplied template metadata and diff content to produce a more professional, audit-friendly LLM prompt.

- BedrockClient is an abstraction for the LLM provider. Tests mock this to avoid external calls.

- SummaryService contains only the orchestration logic for this specific flow. It validates that the engagement exists and short-circuits when current and latest versions are equal.

Testing

- Unit tests are located under src/test/java and use Mockito to mock DiffService and BedrockClient. Tests verify that when a diff exists the service builds a prompt and calls the LLM client; when versions are equal, it returns "No update pending".

Files of interest

- src/main/java/com/caseware/llm/service/SummaryService.java
- src/main/java/com/caseware/llm/service/PromptBuilder.java
- src/main/java/com/caseware/llm/service/DiffService.java
- src/main/java/com/caseware/llm/service/BedrockClient.java
- src/main/java/com/caseware/llm/controller/SummaryController.java

Notes and next steps

- Replace StubDiffService and StubBedrockClient with real integrations (call the template-diff API and Amazon Bedrock).
- Add caching (Redis) and concurrency control to avoid duplicate LLM calls.
- Add observability (metrics, tracing) and error handling strategies (circuit breaker for Bedrock calls).

