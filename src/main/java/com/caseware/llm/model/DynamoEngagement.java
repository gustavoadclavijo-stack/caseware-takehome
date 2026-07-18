package com.caseware.llm.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamoDbBean
public class DynamoEngagement {
    private String companyId;
    private String engagementId;
    private String templateId;
    private String currentTemplateVersion;
    private String latestTemplateVersion;

    @DynamoDbPartitionKey
    public String getCompanyId() { return companyId; }

    public void setCompanyId(String companyId) { this.companyId = companyId; }

    @DynamoDbSortKey
    public String getEngagementId() { return engagementId; }

    public void setEngagementId(String engagementId) { this.engagementId = engagementId; }
}
