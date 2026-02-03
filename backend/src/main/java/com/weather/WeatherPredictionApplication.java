package com.weather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Main Spring Boot Application
 * 
 * Design Patterns Used:
 * - Singleton Pattern: Spring Beans are singletons by default
 * - Factory Pattern: Spring's ApplicationContext acts as a factory
 * - Proxy Pattern: Spring AOP for caching and transactions
 * 
 * 12-Factor App Principles:
 * 1. Codebase - Single codebase tracked in version control
 * 2. Dependencies - Explicitly declared in pom.xml
 * 3. Config - Externalized in application.properties
 * 4. Backing Services - External API treated as attached resource
 * 5. Build, Release, Run - Separated via Maven and Docker
 * 6. Processes - Stateless service
 * 7. Port Binding - Self-contained, exports HTTP via port 8080
 * 8. Concurrency - Horizontally scalable
 * 9. Disposability - Fast startup and graceful shutdown
 * 10. Dev/Prod Parity - Docker ensures consistency
 * 11. Logs - Stdout logging
 * 12. Admin Processes - Actuator endpoints
 */
@SpringBootApplication
@EnableCaching
public class WeatherPredictionApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeatherPredictionApplication.class, args);
    }
}
