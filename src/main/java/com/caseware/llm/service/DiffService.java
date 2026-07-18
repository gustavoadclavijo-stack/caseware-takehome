package com.caseware.llm.service;

import com.caseware.llm.model.DiffResult;

public interface DiffService {
    /**
     * Returns a DiffResult containing lists of added, modified and removed items between two template versions.
     */
    DiffResult computeDiff(String templateId, String fromVersion, String toVersion);
}
