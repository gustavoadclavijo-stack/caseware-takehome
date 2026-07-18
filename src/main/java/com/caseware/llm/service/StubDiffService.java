package com.caseware.llm.service;

import com.caseware.llm.model.DiffResult;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * A stubbed DiffService for local development. In tests this will be mocked.
 * In production this would call the template diff API that returns added/modified/removed.
 */
@Component
public class StubDiffService implements DiffService {

    @Override
    public DiffResult computeDiff(String templateId, String fromVersion, String toVersion) {
        // Very small stub: return a fake diff when versions differ
        if (!fromVersion.equals(toVersion)) {
            return new DiffResult(
                    List.of("Section A"),
                    List.of("Field X changed from 'old' to 'new'"),
                    List.of("Section Z")
            );
        }
        return new DiffResult(List.of(), List.of(), List.of());
    }
}
