package com.caseware.llm.repository;

import com.caseware.llm.model.DynamoEngagement;
import com.caseware.llm.model.Engagement;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.CreateTableEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;

import java.util.Optional;

@Repository
public class DynamoEngagementRepository implements EngagementRepository {

    private static final String TABLE_NAME = "Engagements";

    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<DynamoEngagement> table;

    public DynamoEngagementRepository(DynamoDbEnhancedClient enhancedClient) {
        this.enhancedClient = enhancedClient;
        this.table = enhancedClient.table(TABLE_NAME, TableSchema.fromBean(DynamoEngagement.class));

        // Ensure table exists (for local/dev). In production use migrations or infra tooling.
        try {
            table.createTable(CreateTableEnhancedRequest.builder().billingMode(BillingMode.PAY_PER_REQUEST).build());
        } catch (Exception ex) {
            // Table may already exist; ignore
        }
    }

    @Override
    public Optional<Engagement> findByCompanyIdAndEngagementId(String companyId, String engagementId) {
        DynamoEngagement key = new DynamoEngagement();
        key.setCompanyId(companyId);
        key.setEngagementId(engagementId);
        DynamoEngagement item = table.getItem(r -> r.key(k -> k.partitionValue(companyId).sortValue(engagementId)));
        if (item == null) return Optional.empty();
        Engagement e = new Engagement(item.getCompanyId(), item.getEngagementId(), item.getTemplateId(), item.getCurrentTemplateVersion(), item.getLatestTemplateVersion());
        return Optional.of(e);
    }

    @Override
    public void save(Engagement engagement) {
        DynamoEngagement item = new DynamoEngagement(
                engagement.getCompanyId(),
                engagement.getEngagementId(),
                engagement.getTemplateId(),
                engagement.getCurrentTemplateVersion(),
                engagement.getLatestTemplateVersion()
        );
        table.putItem(item);
    }
}
