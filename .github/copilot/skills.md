# Caseware Template Update Service - Architecture & Context

## Project Overview

This is a **Senior Software Engineer Take-Home Architecture Design Exercise** for a template update notification system. The service maintains engagement-template associations and generates AI-powered summaries of template changes.

---

## Core Architecture Pattern: Event-Driven with Materialized View

The solution uses **Event-Driven Architecture** paired with a **Materialized View** pattern to:
- Minimize engagement file I/O
- Generate AI summaries only on-demand (lazy evaluation)
- Reduce operational costs through intelligent caching

### Key Design Principle
**Separation of Concerns:**
1. **State Synchronization** – Continuous event-driven updates to DynamoDB
2. **Content Generation** – On-demand AI summarization via Amazon Bedrock

---

## Technology Stack

### Cloud Infrastructure (AWS)
- **Amazon SNS**: Event publisher for template publications
- **Amazon SQS**: Durable event queue consumer
- **Amazon DynamoDB**: Materialized view (system of record for engagement-template state)
- **Amazon ElastiCache (Redis)**: Summary caching by template version transition
- **Amazon Bedrock**: LLM for generating human-readable summaries
- **Amazon CloudWatch & X-Ray**: Observability
- **Prometheus + Grafana**: Business metrics dashboards

### Backend
- **Spring Boot**: REST microservice framework
- **Java**: Primary implementation language
- **Resilience4j**: Circuit breaker for Bedrock failure handling
- **Redisson**: Distributed locking to prevent duplicate LLM calls
- **DynamoDB SDK**: Materialized view persistence

### Messaging Events
- `TemplatePublished` – Triggers materialized view updates
- `EngagementCreated` – Initializes engagement record in DynamoDB
- `TemplateDecisionApplied` – Records accept/decline decisions

---

## Data Model

### DynamoDB Principal Table
```
CompanyId (PK)
EngagementId (SK)
├── TemplateId
├── CurrentTemplateVersion
├── LatestTemplateVersion
├── PendingUpdate (boolean)
└── LastUpdated (timestamp)
```

### Global Secondary Index (GSI)
```
TemplateId (PK)
CompanyId#EngagementId (SK)
```
Enables efficient queries of all engagements affected by a template publication.

### Redis Cache Strategy
- **Key**: `{TemplateId}:{CurrentVersion}:{LatestVersion}:summary`
- **Value**: Generated AI summary
- **Hit Scenario**: Same template transition used by multiple engagements
- **Distributed Lock**: Prevents concurrent Bedrock calls for identical transitions

---

## Processing Workflows

### Phase 1: Event Consumption & State Sync
```
SNS Event → SQS Queue → Spring Boot Consumer
↓
Process Event (EngagementCreated / TemplatePublished / TemplateDecisionApplied)
↓
DynamoDB Update (Materialized View)
```

**Key:** Events are processed **asynchronously**; no synchronous dependencies on engagement storage.

---

### Phase 2: Lazy Summary Generation (On-Demand)
```
Frontend Request → REST API
↓
Redis Lookup (Template Transition Key)
├─ HIT: Return cached summary
└─ MISS:
   ├─ Acquire Distributed Lock (Redisson)
   ├─ Retrieve template versions
   ├─ Generate JSON Diff
   ├─ Build LLM Prompt
   ├─ Invoke Amazon Bedrock
   ├─ Cache result (Redis)
   └─ Return summary
```

**Key:** Distributed lock prevents thundering herd for identical transitions.

---

### Phase 3: Decision Handling
```
User Accepts/Declines → REST API
↓
Update CurrentTemplateVersion in DynamoDB
↓
Emit TemplateDecisionApplied event
↓
Clear related Redis cache entries (optional)
```

---

## Critical Implementation Considerations

### 1. Materialized View Synchronization
- **One engagement, multiple pending updates**: Store only `CurrentVersion` → `LatestVersion`
- **Single version comparison** computes all accumulated changes in one AI call
- **Avoid** storing intermediate versions (unnecessary complexity)

### 2. JSON Diff Strategy
- Compute diffs **server-side** to ensure consistency
- Use a diff library (e.g., JsonDiff, IMongo's JSON comparison) to generate structured changes
- Pass diff as context to Bedrock prompt

### 3. LLM Prompt Building
- **Standardize prompts** for reproducibility
- Include engagement context (domain, previous versions)
- Ask Bedrock to output human-readable summaries
- Consider prompt caching for reduced latency

### 4. Failure Modes & Resilience
| Failure Mode | Handling Strategy |
|---|---|
| Bedrock unavailable | Circuit breaker → return JSON diff or degraded message |
| SQS poison message | Auto-move to DLQ after retries |
| DynamoDB hot partition | Use sort key sharding if needed |
| Redis miss under load | Distributed lock + single Bedrock call |
| Network partition | Idempotent event processing (deduplication by event ID) |

### 5. Observability Requirements
- **Metrics to track**:
  - Pending update count by engagement
  - Cache hit ratio (Redis)
  - Bedrock invocation latency
  - Queue depth (SQS)
  - DynamoDB consumed capacity
  - Circuit breaker state transitions

---

## Implementation Phases

### ✅ Phase 1: Materialized View Synchronization
- Consume SNS/SQS events
- Update DynamoDB on `EngagementCreated`, `TemplatePublished`
- Implement idempotent event processing
- Add unit tests for version comparison logic

### ✅ Phase 2: Lazy AI Content Generation
- Implement Redis caching (template transition as key)
- Build prompt generation (JSON diff → human-readable context)
- Integrate Amazon Bedrock
- Add Redisson distributed lock for concurrent calls
- Implement Circuit Breaker for Bedrock failures

### ✅ Phase 3: REST API & Frontend Integration
- `GET /engagements?pending=true` – List engagements with updates
- `GET /engagements/{id}/update-summary` – Retrieve cached or generated summary
- `POST /engagements/{id}/decision` – Accept/Decline update

---

## Testing Strategy

### Unit Tests
- ✅ Materialized view update logic
- ✅ Version comparison (CurrentVersion vs LatestVersion)
- ✅ JSON Diff generation
- ✅ Prompt Builder
- ✅ Cache lookup behavior
- ✅ Idempotent event processing

### Integration Tests
- ✅ SNS → SQS → Event Consumer pipeline
- ✅ Materialized View synchronization accuracy
- ✅ Redis caching and TTL
- ✅ Bedrock integration (mocked in CI/CD)
- ✅ DynamoDB GSI queries

### Load & Concurrency Tests
- ✅ Concurrent requests for same template transition
- ✅ Distributed lock effectiveness
- ✅ DynamoDB throughput under peak load
- ✅ SQS batch processing performance

---

## Code Organization Guidelines

### Suggested Package Structure
```
com.caseware.template.update
├── api/
│   └── EngagementController.java
├── service/
│   ├── EngagementService.java
│   ├── SummaryGenerationService.java
│   └── TemplateUpdateService.java
├── event/
│   ├── TemplatePublishedEventConsumer.java
│   ├── EngagementCreatedEventConsumer.java
│   └── EventDeserializer.java
├── model/
│   ├── Engagement.java
│   ├── TemplateVersion.java
│   └── UpdateSummary.java
├── repository/
│   └── EngagementDynamoDBRepository.java
├── cache/
│   └── RedisCache.java
├── bedrock/
│   └── BedrockClient.java
├── prompt/
│   └── PromptBuilder.java
└── config/
    ├── DynamoDBConfig.java
    ├── RedisConfig.java
    └── BedrockConfig.java
```

---

## Key Metrics & KPIs

Monitor and expose:
- **Pending Updates**: Count of engagements with available updates
- **Cache Hit Ratio**: Percentage of summaries served from Redis (target: >70%)
- **Bedrock Latency**: P50, P95, P99 for summary generation
- **Cost per Summary**: Track AI inference costs vs engagement volume
- **Queue Latency**: SQS message age in queue

---

## Known Tradeoffs

| Decision | Benefit | Tradeoff |
|---|---|---|
| Lazy LLM generation | Reduced AI costs | First user may experience delay |
| Materialized view over live queries | Fast lookups, no I/O | Must maintain eventual consistency |
| Summary caching by transition | High hit ratio | Invalidation complexity on version changes |
| Distributed lock on Bedrock calls | Single invocation per transition | Slight increased latency on lock acquisition |

---

## When to Use This Context

This skill should be referenced when:
- ✅ Adding new event types to the consumer
- ✅ Implementing API endpoints
- ✅ Optimizing database queries or caching
- ✅ Debugging message processing failures
- ✅ Designing monitoring/alerting rules
- ✅ Adding new failure handling scenarios
- ✅ Scaling DynamoDB or Redis
- ✅ Reviewing code for architectural alignment

---

## References

- Architecture document: `arch1_final.md`
- Primary language: **Java** (100% of codebase)
- Build framework: **Spring Boot**
- Infrastructure: **AWS** (SNS, SQS, DynamoDB, ElastiCache, Bedrock)
