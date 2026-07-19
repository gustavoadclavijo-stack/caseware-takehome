package com.caseware.llm.service;

import com.caseware.llm.model.DiffResult;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builds a structured prompt for summarizing template changes using a template resource and placeholders.
 */
@Component
public class PromptBuilder {

    private static final String PROMPT_TEMPLATE_PATH = "prompts/template-summary-prompt.txt";
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{(\\w+)\\}\\}");

    public String buildPrompt(String templateId, String fromVersion, String toVersion, DiffResult diff) {
        String template = loadTemplate();
        Map<String, String> values = new LinkedHashMap<>();
        values.put("templateId", safeValue(templateId));
        values.put("fromVersion", safeValue(fromVersion));
        values.put("toVersion", safeValue(toVersion));
        values.put("changesSection", buildChangesSection(diff));

        return replacePlaceholders(template, values);
    }

    private String loadTemplate() {
        try {
            Resource resource = new ClassPathResource(PROMPT_TEMPLATE_PATH);
            try (InputStream inputStream = resource.getInputStream()) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load prompt template from resources", ex);
        }
    }

    private String replacePlaceholders(String template, Map<String, String> values) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1);
            String replacement = values.getOrDefault(key, "");
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private String buildChangesSection(DiffResult diff) {
        if (diff == null) {
            return "No functional changes detected.";
        }

        boolean hasChanges = (diff.getAdded() != null && !diff.getAdded().isEmpty())
                || (diff.getModified() != null && !diff.getModified().isEmpty())
                || (diff.getRemoved() != null && !diff.getRemoved().isEmpty());

        if (!hasChanges) {
            return "No functional changes detected.";
        }

        StringBuilder sb = new StringBuilder();

        if (diff.getAdded() != null && !diff.getAdded().isEmpty()) {
            sb.append("Added items:\n");
            diff.getAdded().forEach(item -> sb.append("- ").append(item).append("\n"));
            sb.append("\n");
        }

        if (diff.getModified() != null && !diff.getModified().isEmpty()) {
            sb.append("Modified items:\n");
            diff.getModified().forEach(item -> sb.append("- ").append(item).append("\n"));
            sb.append("\n");
        }

        if (diff.getRemoved() != null && !diff.getRemoved().isEmpty()) {
            sb.append("Removed items:\n");
            diff.getRemoved().forEach(item -> sb.append("- ").append(item).append("\n"));
        }

        return sb.toString().trim();
    }

    private String safeValue(String value) {
        return value == null ? "N/A" : value;
    }
}
