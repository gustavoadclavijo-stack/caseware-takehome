package com.caseware.llm.service;

import com.caseware.llm.model.DiffResult;
import com.caseware.llm.model.Engagement;
import com.caseware.llm.repository.EngagementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.testcontainers.containers.GenericContainer;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.CreateTableEnhancedRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Integration test that starts a DynamoDB Local container and verifies the repository + service flow.
 * This test requires Docker. It's skipped if the system property 'skipDockerTests' is set to true.
 */
@DisabledIfSystemProperty(named = "skipDockerTests", matches = "true")
public class DynamoIntegrationTest {

    private GenericContainer<?> dynamo;
    private DynamoDbClient client;
    private DynamoDbEnhancedClient enhancedClient;

    @BeforeEach
    void setUp() {
        dynamo = new GenericContainer<>("amazon/dynamodb-local:latest").withExposedPorts(8000);
        dynamo.start();

        String endpoint = String.format("http://%s:%d", dynamo.getHost(), dynamo.getMappedPort(8000));

        client = DynamoDbClient.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("x", "y")))
                .build();

        enhancedClient = DynamoDbEnhancedClient.builder().dynamoDbClient(client).build();

        // create table
        DynamoDbTable<?> table = enhancedClient.table("Engagements", TableSchema.fromClass(com.caseware.llm.model.DynamoEngagement.class));
        try {
            table.createTable(CreateTableEnhancedRequest.builder().build());
        } catch (Exception ex) {
            // ignore
        }
    }

    @AfterEach
    void tearDown() {
        if (client != null) client.close();
        if (dynamo != null) dynamo.stop();
    }

    @Test
    void fullFlow_withDynamo_shouldGenerateSummary() {
        // Build repository pointing to local dynamo
        com.caseware.llm.repository.DynamoEngagementRepository repo = new com.caseware.llm.repository.DynamoEngagementRepository(enhancedClient);

        // Seed data
        repo.save(new Engagement("c1", "e1", "t1", "1.0", "1.2"));

        // Mock external services
        DiffService diffService = mock(DiffService.class);
        BedrockClient bedrockClient = mock(BedrockClient.class);

        DiffResult diff = new DiffResult(
                List.of("Added A"),
                List.of("Modified B"),
                List.of("Removed C")
        );
        when(diffService.computeDiff("t1", "1.0", "1.2")).thenReturn(diff);
        when(bedrockClient.generateSummary(anyString())).thenReturn("Human summary from LLM");

        PromptBuilder promptBuilder = new PromptBuilder();
        SummaryService summaryService = new SummaryService(repo, diffService, promptBuilder, bedrockClient);

        String result = summaryService.generateHumanReadableSummary("c1", "e1");

        assertThat(result).isEqualTo("Human summary from LLM");
        verify(diffService, times(1)).computeDiff("t1", "1.0", "1.2");
        verify(bedrockClient, times(1)).generateSummary(anyString());
    }
}
