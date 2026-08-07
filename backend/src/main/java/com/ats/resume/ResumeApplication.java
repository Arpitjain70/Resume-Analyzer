package com.ats.resume;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ─────────────────────────────────────────────────────────
 * ResumeApplication — The Entry Point
 * ─────────────────────────────────────────────────────────
 *
 * WHY THIS FILE EXISTS:
 * Every Java application needs a main() method as its starting point.
 * Spring Boot also needs to know which package to scan for components
 * (controllers, services, repositories). This class tells it both.
 *
 * WHAT @SpringBootApplication DOES:
 * It is a combination of three annotations:
 *
 *   @Configuration      → This class can define Spring beans (objects Spring manages)
 *   @EnableAutoConfiguration → Spring Boot auto-configures things like DataSource,
 *                              Security, JPA based on what's on the classpath
 *   @ComponentScan      → Scans all classes in 'com.ats.resume' package and sub-packages
 *                         for @Component, @Service, @Repository, @Controller, etc.
 *
 * STARTUP FLOW:
 * 1. JVM starts → calls main()
 * 2. SpringApplication.run() bootstraps the Spring context
 * 3. Auto-configuration kicks in (creates DataSource, sets up Security, etc.)
 * 4. All @Component classes are instantiated and wired together
 * 5. Embedded Tomcat starts listening on port 8080
 */
@SpringBootApplication
public class ResumeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResumeApplication.class, args);
    }
}
