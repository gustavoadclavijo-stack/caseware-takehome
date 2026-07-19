package com.caseware.llm.repository;

import com.caseware.llm.model.Engagement;

import java.util.Optional;

public interface EngagementRepository {
    Optional<Engagement> findByCompanyIdAndEngagementId(String companyId, String engagementId);

    void save(Engagement engagement);
}
