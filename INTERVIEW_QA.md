# Interview Questions & Answers
## Weather Prediction Microservice

This document contains common questions a senior software engineer might ask during your presentation, along with detailed answers.

---

## Architecture & Design

### Q1: Why did you choose microservices architecture for this project?

**Answer**: 
I chose microservices architecture for several reasons:
1. **Separation of Concerns**: Frontend and backend are independently deployable
2. **Scalability**: Each service can scale independently based on load
3. **Technology Flexibility**: Can use different tech stacks (React + Spring Boot)
4. **Fault Isolation**: If one service fails, others continue running
5. **Independent Development**: Teams can work on frontend/backend separately

The backend is a self-contained microservice exposing RESTful APIs that can be consumed by any client (web, mobile, etc.).

---

### Q2: Explain the design patterns you've used and why.

**Answer**:

1. **Strategy Pattern** (`WeatherWarningStrategy`)
   - **Why**: Need to add new warning conditions without modifying existing code
   - **Example**: Easy to add snow warning, heat wave warning, etc.
   - **Benefit**: Open/Closed Principle - open for extension, closed for modification

2. **Facade Pattern** (`WeatherService`)
   - **Why**: Simplifies complex interactions between multiple providers and strategies
   - **Benefit**: Clients interact with one simple interface

3. **Factory Pattern** (Spring's `ApplicationContext`)
   - **Why**: Centralized object creation and dependency management
   - **Benefit**: Loose coupling

4. **Template Method** (`WeatherDataProvider`)
   - **Why**: Common algorithm structure with variable implementation details
   - **Benefit**: Code reuse while allowing customization

5. **Proxy Pattern** (Spring's `@Cacheable`)
   - **Why**: Add caching without modifying service code
   - **Benefit**: Cross-cutting concerns handled separately

---

### Q3: How does your offline mode work? Walk me through the flow.

**Answer**:

**Normal Flow**:
1. User requests weather for London
2. `WeatherService` checks `offlineModeEnabled` flag (default: false)
3. Calls `OnlineWeatherDataProvider`
4. Makes HTTP request to OpenWeather API
5. Transforms API response to domain model
6. Applies warning strategies
7. Returns response with `dataSource: "online"`

**Offline Mode Flow**:
1. User toggles offline mode via `/api/v1/weather/offline-mode?enabled=true`
2. `WeatherService` sets `offlineModeEnabled = true`
3. Next forecast request goes to `OfflineWeatherDataProvider`
4. Loads data from `offline-weather-data.json` (loaded at startup)
5. Returns response with `dataSource: "offline"` and message

**Automatic Fallback**:
- If online API fails (timeout, 500 error, network issue)
- `WeatherService` catches exception
- Automatically falls back to `OfflineWeatherDataProvider`
- Returns cached data with warning message

This ensures the service is always available, even when the external API is down.

---

## SOLID Principles

### Q4: How does your code follow SOLID principles? Give examples.

**Answer**:

**1. Single Responsibility Principle**
- `WeatherService`: Only coordinates weather data fetching
- `OnlineWeatherDataProvider`: Only fetches from API
- `ThunderstormWarningStrategy`: Only handles thunderstorm warnings
- Each class has ONE reason to change

**2. Open/Closed Principle**
```java
// Adding new warning is easy - no modification needed
@Component
public class SnowWarningStrategy implements WeatherWarningStrategy {
    public boolean applies(WeatherForecast forecast) {
        return forecast.hasSnow();
    }
    public String getWarningMessage() {
        return "Heavy snow expected - drive carefully!";
    }
}
```

**3. Liskov Substitution**
- Any `WeatherDataProvider` can substitute another
- `WeatherService` works with `OnlineWeatherDataProvider` OR `OfflineWeatherDataProvider`
- No behavior change, just data source

**4. Interface Segregation**
- `WeatherDataProvider`: Clean, focused interface (3 methods)
- `WeatherWarningStrategy`: Simple contract (3 methods)
- No "fat interfaces" with unused methods

**5. Dependency Inversion**
```java
// WeatherService depends on abstraction, not concrete classes
private final WeatherDataProvider onlineProvider;  // ← Interface
private final WeatherDataProvider offlineProvider; // ← Interface
```

---

## API Design

### Q5: Explain HATEOAS and how you implemented it.

**Answer**:

**HATEOAS** = Hypermedia as the Engine of Application State

**Concept**: API responses include hypermedia links to related resources, making the API self-documenting and discoverable.

**My Implementation**:
```java
@RestController
public class WeatherController {
    @GetMapping("/forecast")
    public ResponseEntity<WeatherResponse> getWeatherForecast(@RequestParam String city) {
        WeatherResponse response = weatherService.getWeatherForecast(city);
        
        // Add HATEOAS links
        Link selfLink = linkTo(methodOn(WeatherController.class)
            .getWeatherForecast(city))
            .withSelfRel();
        response.add(selfLink);
        
        Link offlineModeLink = linkTo(methodOn(WeatherController.class)
            .toggleOfflineMode(false))
            .withRel("toggle-offline-mode");
        response.add(offlineModeLink);
        
        return ResponseEntity.ok(response);
    }
}
```

**Response Example**:
```json
{
  "city": "London",
  "forecasts": [...],
  "_links": {
    "self": { "href": "/api/v1/weather/forecast?city=London" },
    "toggle-offline-mode": { "href": "/api/v1/weather/offline-mode" },
    "health": { "href": "/api/v1/weather/health" }
  }
}
```

**Benefits**:
1. Clients discover available actions from responses
2. API becomes self-documenting
3. Clients don't hardcode URLs
4. Easy to add new endpoints without breaking clients

---

### Q6: How would you handle API rate limiting from OpenWeather?

**Answer**:

**Current Implementation**: Basic caching with `@Cacheable`

**Enhanced Solution**:

1. **Token Bucket Algorithm**:
```java
@Service
public class RateLimiter {
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    
    public boolean allowRequest(String key) {
        TokenBucket bucket = buckets.computeIfAbsent(key, 
            k -> new TokenBucket(60, 1, TimeUnit.MINUTES)); // 60 requests/minute
        return bucket.tryConsume();
    }
}
```

2. **Queue Requests**:
- If rate limit hit, queue request
- Process when tokens available
- Return cached data immediately if available

3. **Response Headers**:
```java
response.setHeader("X-RateLimit-Limit", "60");
response.setHeader("X-RateLimit-Remaining", remaining);
response.setHeader("X-RateLimit-Reset", resetTime);
```

4. **Backoff Strategy**:
- Exponential backoff on 429 (Too Many Requests)
- Retry with increasing delays: 1s, 2s, 4s, 8s

5. **Multiple API Keys**:
- Rotate between multiple keys
- Distribute load

---

## Testing & Quality

### Q7: Explain your testing strategy. How do you ensure quality?

**Answer**:

**Testing Pyramid**:

**1. Unit Tests** (70% of tests)
```java
@Test
void testHighTemperatureWarning() {
    // Given
    WeatherForecast forecast = createForecastWithTemp(42.0);
    
    // When
    boolean applies = strategy.applies(forecast);
    
    // Then
    assertTrue(applies);
    assertEquals("Use sunscreen lotion", strategy.getWarningMessage());
}
```
- Fast, isolated tests
- Mock external dependencies
- Test business logic

**2. Integration Tests** (20%)
```java
@WebMvcTest(WeatherController.class)
class WeatherControllerTest {
    @Test
    void testGetWeatherForecast() throws Exception {
        mockMvc.perform(get("/api/v1/weather/forecast")
            .param("city", "London"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.city").value("London"));
    }
}
```
- Test API endpoints
- Test service integration
- Use test containers for real dependencies

**3. E2E Tests** (10%)
- Selenium/Cypress for UI flows
- Test complete user journeys

**Code Coverage**: 85%+ (measured with JaCoCo)

**Quality Gates**:
- Code must compile
- All tests must pass
- Coverage > 80%
- No critical security vulnerabilities
- SonarQube quality gate passed

---

### Q8: How do you handle errors and exceptions?

**Answer**:

**Layered Error Handling**:

**1. Global Exception Handler**:
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(WeatherServiceException.class)
    public ResponseEntity<ErrorResponse> handleWeatherException(
        WeatherServiceException ex) {
        return ResponseEntity
            .status(INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
    }
}
```

**2. Custom Exceptions**:
```java
public class WeatherServiceException extends RuntimeException {
    private final String errorCode;
    // Provides context for debugging
}
```

**3. Graceful Degradation**:
- If API fails → fallback to offline data
- If offline data unavailable → return clear error message
- Never expose stack traces to clients

**4. Logging**:
```java
log.error("API call failed for city: {}", city, exception);
// Structured logging for debugging
```

**5. Error Response Format**:
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 500,
  "errorCode": "API_ERROR",
  "message": "Failed to fetch weather data",
  "path": "/api/v1/weather/forecast"
}
```

**Benefits**:
- Consistent error format
- Easy debugging
- Client-friendly messages
- No information leakage

---

## Security

### Q9: How do you secure sensitive information like API keys?

**Answer**:

**1. Jasypt Encryption**:
```properties
# application.properties
weather.api.key=ENC(d2929e9483efc82c82c32ee7e02d563e)
jasypt.encryptor.password=${JASYPT_PASSWORD}
```

**Encryption Process**:
```bash
# Generate encrypted value
java -cp jasypt-1.9.3.jar \
  org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI \
  input="myApiKey" \
  password="secretPassword" \
  algorithm=PBEWithMD5AndDES
```

**2. Environment Variables**:
```yaml
# docker-compose.yml
environment:
  - JASYPT_PASSWORD=${JASYPT_PASSWORD}
```

**3. Secret Management** (Production):
- AWS Secrets Manager
- HashiCorp Vault
- Kubernetes Secrets

**4. Never Commit Secrets**:
```gitignore
*.env
application-prod.properties
secrets/
```

**5. Principle of Least Privilege**:
- API key has minimal required permissions
- Read-only access to weather data

---

### Q10: What security vulnerabilities did you consider?

**Answer**:

**1. SQL Injection**: N/A (no database)

**2. XSS (Cross-Site Scripting)**:
- React automatically escapes output
- No `dangerouslySetInnerHTML` used

**3. CSRF (Cross-Site Request Forgery)**:
- Stateless API (no cookies)
- Token-based would use CSRF tokens

**4. Sensitive Data Exposure**:
- API keys encrypted
- No PII stored
- HTTPS in production

**5. Security Misconfiguration**:
```nginx
# nginx.conf
add_header X-Frame-Options "SAMEORIGIN";
add_header X-Content-Type-Options "nosniff";
add_header X-XSS-Protection "1; mode=block";
```

**6. Dependency Vulnerabilities**:
```bash
# Maven dependency check
mvn dependency-check:check

# Docker image scanning
trivy image weather-backend:latest
```

**7. DoS Protection**:
- Rate limiting (can be added)
- Request timeout configured
- Connection pool limits

---

## DevOps & Deployment

### Q11: Walk me through your CI/CD pipeline.

**Answer**:

**Pipeline Stages**:

**1. Checkout** (30s)
- Clone repository
- Checkout branch

**2. Build** (2-3 min)
- Compile Java code: `mvn compile`
- Install npm dependencies: `npm ci`

**3. Test** (3-5 min)
- Unit tests: `mvn test`
- Integration tests: `mvn verify`
- Frontend tests: `npm test`
- Generate coverage reports (JaCoCo)

**4. Quality Gate** (2 min)
- SonarQube analysis
- Check code coverage > 80%
- Check code smells, bugs, vulnerabilities
- Block if quality gate fails

**5. Package** (2 min)
- Create JAR: `mvn package`
- Build React: `npm run build`
- Archive artifacts

**6. Docker Build** (3-5 min)
- Build backend image
- Build frontend image
- Tag with build number

**7. Security Scan** (2-3 min)
- Trivy vulnerability scanning
- Fail on HIGH/CRITICAL vulnerabilities

**8. Push to Registry** (1-2 min)
- Push to Docker Hub/ECR
- Tag as 'latest' and version

**9. Deploy** (2-3 min)
- `docker-compose down`
- `docker-compose up -d`
- Rolling update in production

**10. Health Check** (1 min)
- Wait 30s for startup
- Check `/actuator/health`
- Check frontend availability

**Total Time**: ~15-20 minutes

**Notifications**:
- Slack on success/failure
- Email on failure

---

### Q12: How do you ensure zero-downtime deployments?

**Answer**:

**Current**: `docker-compose` with restart policies

**Production Strategy**:

**1. Blue-Green Deployment**:
```yaml
services:
  backend-blue:
    image: weather-backend:v1
  backend-green:
    image: weather-backend:v2
  
  load-balancer:
    # Switch traffic blue → green
```

**2. Rolling Update** (Kubernetes):
```yaml
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 1  # Keep 2 running
      maxSurge: 1        # Add 1 new pod
```

**3. Health Checks**:
```yaml
healthcheck:
  test: ["CMD", "wget", "--spider", "http://localhost:8080/actuator/health"]
  interval: 30s
  timeout: 10s
  retries: 3
  start_period: 40s
```

**4. Graceful Shutdown**:
```properties
server.shutdown=graceful
spring.lifecycle.timeout-per-shutdown-phase=30s
```

**Process**:
1. Start new version (green)
2. Wait for health check (30s)
3. Switch traffic to green
4. Drain connections from blue
5. Stop blue after 30s
6. Rollback if issues detected

---

## Performance & Scalability

### Q13: How would you scale this application for 1 million users?

**Answer**:

**Current**: Single instance

**Scalability Strategy**:

**1. Horizontal Scaling**:
```yaml
# Kubernetes
replicas: 10  # Multiple backend pods
```

**2. Load Balancing**:
```
           ┌─── Backend Pod 1
           │
LB (Nginx) ├─── Backend Pod 2
           │
           └─── Backend Pod 3
```

**3. Caching Layers**:

**Level 1 - Application Cache** (Current):
```java
@Cacheable("weather-forecasts")  // In-memory
```

**Level 2 - Distributed Cache**:
```java
@Cacheable("weather-forecasts")  // Redis
TTL: 1 hour
```

**Level 3 - CDN**:
- Cache API responses
- Edge locations worldwide
- Reduce latency

**4. Database** (if needed):
```
Read Replicas:
  Master (Write) → Replica 1 (Read)
                 → Replica 2 (Read)
                 → Replica 3 (Read)
```

**5. API Gateway**:
- Rate limiting per user
- Request throttling
- Circuit breaker for external APIs

**6. Async Processing**:
```java
@Async
public CompletableFuture<WeatherResponse> getWeatherAsync(String city) {
    // Non-blocking
}
```

**7. Monitoring**:
- Prometheus metrics
- Grafana dashboards
- Alerts on high latency, errors

**Expected Performance**:
- Latency: <100ms (cached), <500ms (uncached)
- Throughput: 10,000 requests/second
- Availability: 99.9%

---

### Q14: How do you monitor application health in production?

**Answer**:

**1. Spring Boot Actuator**:
```
/actuator/health     - Overall health
/actuator/metrics    - JVM, HTTP metrics
/actuator/prometheus - Prometheus format
```

**2. Metrics Collected**:
- Request count, latency (p50, p95, p99)
- Error rate
- JVM memory, CPU usage
- Thread pool size
- Cache hit/miss ratio

**3. Alerting**:
```yaml
# Prometheus alert rules
- alert: HighErrorRate
  expr: rate(http_requests_total{status="500"}[5m]) > 0.05
  for: 5m
  annotations:
    summary: "High error rate detected"
```

**4. Logging**:
```java
// Structured logging
log.info("Weather forecast requested", 
    kv("city", city),
    kv("userId", userId),
    kv("duration", duration));
```

**5. Distributed Tracing** (Zipkin/Jaeger):
- Trace request across services
- Identify bottlenecks

**6. Dashboards** (Grafana):
- Real-time metrics visualization
- Historical trends
- Anomaly detection

**7. Synthetic Monitoring**:
- Ping endpoints every minute
- Alert if unavailable
- Test from multiple regions

---

## Advanced Questions

### Q15: How would you implement real-time weather updates?

**Answer**:

**Current**: Pull model (user requests)

**Real-time Solution**:

**1. WebSocket**:
```java
@Controller
public class WeatherWebSocketController {
    @MessageMapping("/weather/subscribe")
    @SendTo("/topic/weather/{city}")
    public WeatherUpdate subscribeToCity(String city) {
        // Send updates when weather changes
    }
}
```

**Frontend**:
```javascript
const socket = new WebSocket('ws://localhost:8080/ws');
socket.onmessage = (event) => {
    const weather = JSON.parse(event.data);
    updateUI(weather);
};
```

**2. Server-Sent Events (SSE)**:
```java
@GetMapping(value = "/weather/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<WeatherUpdate> streamWeather(@RequestParam String city) {
    return weatherService.getWeatherStream(city);
}
```

**3. Polling with Long-Polling**:
```javascript
async function longPoll() {
    const response = await fetch('/api/v1/weather/updates?timeout=30s');
    const updates = await response.json();
    updateUI(updates);
    longPoll();  // Next poll
}
```

**4. Push Notifications** (Mobile):
```java
@Service
public class WeatherNotificationService {
    public void sendAlert(String userId, String message) {
        firebaseMessaging.send(Message.builder()
            .setToken(deviceToken)
            .setNotification(Notification.builder()
                .setTitle("Weather Alert")
                .setBody(message)
                .build())
            .build());
    }
}
```

**Best Choice**: WebSocket for web, Push for mobile

---

### Q16: How would you handle multiple weather APIs for redundancy?

**Answer**:

**Strategy**: Circuit Breaker + Fallback Chain

**Implementation**:

**1. Multiple Providers**:
```java
@Service
public class OpenWeatherProvider implements WeatherDataProvider { }

@Service
public class WeatherAPIProvider implements WeatherDataProvider { }

@Service
public class WeatherBitProvider implements WeatherDataProvider { }
```

**2. Circuit Breaker** (Resilience4j):
```java
@CircuitBreaker(name = "openweather", fallbackMethod = "fallbackToWeatherAPI")
public WeatherResponse getFromOpenWeather(String city) {
    return openWeatherProvider.getWeatherForecast(city);
}

private WeatherResponse fallbackToWeatherAPI(String city, Exception e) {
    return weatherAPIProvider.getWeatherForecast(city);
}
```

**3. Fallback Chain**:
```
Try OpenWeather API
  ↓ (fails)
Try WeatherAPI.com
  ↓ (fails)
Try WeatherBit API
  ↓ (fails)
Use Offline Data
  ↓ (fails)
Return Error
```

**4. Health Monitoring**:
```java
@Scheduled(fixedRate = 60000)  // Every minute
public void checkProvidersHealth() {
    providers.forEach(provider -> {
        boolean healthy = provider.healthCheck();
        metrics.gauge("provider.health", healthy ? 1 : 0, 
            Tags.of("provider", provider.getName()));
    });
}
```

**5. Load Balancing**:
```java
public WeatherResponse getWeatherForecast(String city) {
    List<WeatherDataProvider> healthyProviders = getHealthyProviders();
    WeatherDataProvider provider = loadBalancer.select(healthyProviders);
    return provider.getWeatherForecast(city);
}
```

**Benefits**:
- High availability (99.99%)
- Automatic failover
- No single point of failure

---

### Q17: Explain your data transformation from API to domain model.

**Answer**:

**Problem**: OpenWeather API returns complex nested JSON. We need clean domain model.

**Transformation**:

**Step 1 - Parse API Response**:
```java
OpenWeatherResponse apiResponse = webClient.get()
    .uri("/forecast?q={city}", city)
    .retrieve()
    .bodyToMono(OpenWeatherResponse.class)
    .block();
```

**Step 2 - Group by Date**:
```java
Map<LocalDate, List<ForecastItem>> forecastsByDate = 
    apiResponse.getList().stream()
        .collect(Collectors.groupingBy(item -> 
            Instant.ofEpochSecond(item.getDt())
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        ));
```

**Step 3 - Aggregate Daily Data**:
```java
List<WeatherForecast> forecasts = forecastsByDate.entrySet().stream()
    .sorted(Map.Entry.comparingByKey())
    .limit(3)  // Next 3 days
    .map(entry -> {
        List<ForecastItem> dailyItems = entry.getValue();
        return WeatherForecast.builder()
            .date(entry.getKey())
            .tempMax(dailyItems.stream()
                .mapToDouble(i -> i.getMain().getTemp_max())
                .max().orElse(0.0))
            .tempMin(dailyItems.stream()
                .mapToDouble(i -> i.getMain().getTemp_min())
                .min().orElse(0.0))
            .windSpeed(dailyItems.stream()
                .mapToDouble(i -> i.getWind().getSpeed())
                .average().orElse(0.0))
            .hasRain(dailyItems.stream()
                .anyMatch(i -> i.getWeather().stream()
                    .anyMatch(w -> w.getMain().contains("Rain"))))
            .build();
    })
    .collect(Collectors.toList());
```

**Why This Approach**:
- Clean separation (API model vs Domain model)
- Aggregates 3-hour forecasts into daily
- Handles missing data gracefully
- Type-safe with Java Streams

---

## General Best Practices

### Q18: What improvements would you make if you had more time?

**Answer**:

**High Priority**:
1. **Authentication & Authorization**
   - JWT tokens
   - User-specific forecasts
   - Rate limiting per user

2. **Enhanced Caching**
   - Redis distributed cache
   - Cache invalidation strategy
   - TTL per city (busy cities = shorter TTL)

3. **Database Integration**
   - Store user preferences
   - Historical weather data
   - Analytics

4. **Advanced Monitoring**
   - Distributed tracing (Jaeger)
   - Real-time dashboards (Grafana)
   - Alerting (PagerDuty)

**Medium Priority**:
5. **GraphQL API**
   - Let clients query what they need
   - Reduce over-fetching

6. **Machine Learning**
   - Predict weather patterns
   - Personalized warnings based on user behavior

7. **Mobile App**
   - React Native
   - Push notifications

**Low Priority**:
8. **Internationalization (i18n)**
   - Multi-language support
   - Different temperature units (F/C/K)

9. **Dark Mode**
   - UI theme toggle

10. **Export Functionality**
    - Download forecast as PDF/CSV

---

### Q19: How do you stay updated with technology trends?

**Answer**:

**Regular Activities**:
1. **Reading**:
   - Spring Blog
   - Medium articles
   - Hacker News
   - Reddit r/programming

2. **Hands-on Learning**:
   - Side projects (like this!)
   - Open source contributions
   - Code katas

3. **Community**:
   - Local meetups
   - Tech conferences
   - Online forums (Stack Overflow)

4. **Courses**:
   - Udemy, Coursera
   - YouTube tutorials
   - Official documentation

5. **Experimentation**:
   - Try new libraries/frameworks
   - Compare alternatives
   - Read release notes

**Recent Topics I've Explored**:
- Spring Boot 3 and Java 17 features
- Docker multi-stage builds
- React Hooks and Context API
- Microservices patterns (Circuit Breaker, API Gateway)
- Observability (Logs, Metrics, Traces)

---

### Q20: Any questions for me about the role or team?

**Answer**:

Great questions to ask:

**About the Team**:
1. What's the team structure and size?
2. How do you handle code reviews?
3. What's your deployment frequency?
4. How do you balance technical debt vs new features?

**About Technology**:
5. What's your current tech stack?
6. Are you planning any major migrations?
7. How do you handle legacy code?

**About Process**:
8. What's your sprint cycle?
9. How do you prioritize work?
10. What's your testing strategy?

**About Growth**:
11. What opportunities for learning exist?
12. How do you support career growth?
13. What's the most challenging project the team is working on?

---

## Tips for the Presentation

### Do's:
✅ Start with architecture diagram
✅ Demo the application live
✅ Walk through code examples
✅ Explain your thought process
✅ Be honest about tradeoffs
✅ Show enthusiasm for technology
✅ Ask clarifying questions

### Don'ts:
❌ Memorize scripts - understand concepts
❌ Rush through explanation
❌ Ignore the "why" behind decisions
❌ Say "I don't know" without elaborating
❌ Over-promise capabilities
❌ Blame requirements for shortcuts

### Presentation Flow (30 min):
1. **Overview** (3 min): Problem statement, solution
2. **Demo** (5 min): Live application walkthrough
3. **Architecture** (5 min): Diagrams, flow
4. **Code Deep-Dive** (7 min): Key patterns, decisions
5. **Testing & Quality** (3 min): Strategy, coverage
6. **DevOps** (4 min): CI/CD, Docker
7. **Q&A** (remaining time)

### Body Language:
- Maintain eye contact
- Speak clearly and confidently
- Use hand gestures naturally
- Stand/sit comfortably
- Smile and be personable

Good luck with your interview! 🚀
