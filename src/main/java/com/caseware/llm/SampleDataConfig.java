package com.caseware.llm;

import com.caseware.llm.model.Engagement;
import com.caseware.llm.repository.EngagementRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sample data loader to simulate DynamoDB materialized view in local/dev.
 */
@Configuration
public class SampleDataConfig {

    @Bean
    CommandLineRunner runner(EngagementRepository repo) {
        return args -> {
            // Seed two engagements
            repo.save(new Engagement("companyA", "eng-1", "template-foo", "1.0", "1.2"));
            repo.save(new Engagement("companyA", "eng-2", "template-bar", "2.0", "2.0"));
        };
    }
}
