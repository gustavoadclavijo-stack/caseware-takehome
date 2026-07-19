package com.caseware.llm.model;

public class Engagement {
    private final String companyId;
    private final String engagementId;
    private final String templateId;
    private final String currentTemplateVersion;
    private final String latestTemplateVersion;

    public Engagement(String companyId, String engagementId, String templateId, String currentTemplateVersion, String latestTemplateVersion) {
        this.companyId = companyId;
        this.engagementId = engagementId;
        this.templateId = templateId;
        this.currentTemplateVersion = currentTemplateVersion;
        this.latestTemplateVersion = latestTemplateVersion;
    }

    public String getCompanyId() {
        return companyId;
    }

    public String getEngagementId() {
        return engagementId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public String getCurrentTemplateVersion() {
        return currentTemplateVersion;
    }

    public String getLatestTemplateVersion() {
        return latestTemplateVersion;
    }
}
