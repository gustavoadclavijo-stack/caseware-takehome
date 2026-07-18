package com.caseware.llm.service;

import com.caseware.llm.model.DiffResult;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * A simple Prompt Builder which converts a DiffResult into a human-friendly prompt for the LLM.
 * In a real system this would include more context, examples and instructions for tone/format.
 */
@Component
public class PromptBuilder {

    public String buildPrompt(String templateId, String fromVersion, String toVersion, DiffResult diff) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an assistant that explains template changes in plain English.\n");
        sb.append(String.format("Template: %s\nFrom: %s -> To: %s\n\n", templateId, fromVersion, toVersion));

        if ((diff.getAdded() == null || diff.getAdded().isEmpty())
                && (diff.getModified() == null || diff.getModified().isEmpty())
                && (diff.getRemoved() == null || diff.getRemoved().isEmpty())) {
            sb.append("No functional changes detected.\n");
            return sb.toString();
        }

        if (diff.getAdded() != null && !diff.getAdded().isEmpty()) {
            sb.append("Added items:\n");
            diff.getAdded().forEach(a -> sb.append(" - ").append(a).append("\n"));
            sb.append("\n");
        }

        if (diff.getModified() != null && !diff.getModified().isEmpty()) {
            sb.append("Modified items:\n");
            diff.getModified().forEach(m -> sb.append(" - ").append(m).append("\n"));
            sb.append("\n");
        }

        if (diff.getRemoved() != null && !diff.getRemoved().isEmpty()) {
            sb.append("Removed items:\n");
            diff.getRemoved().forEach(r -> sb.append(" - ").append(r).append("\n"));
            sb.append("\n");
        }

        sb.append("Please provide a concise human-readable summary that a non-technical auditor can use to decide whether to apply these changes.\n");
        return sb.toString();
    }
}
