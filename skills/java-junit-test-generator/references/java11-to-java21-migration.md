# Java 11 / 17 to Java 21 Spring Boot Testing Migration Guide

## 1. Namespace Dual-Compatibility Rules (javax vs jakarta)

- **Java 11 / Spring Boot 2.7**: Uses `javax.validation.constraints.*`, `javax.persistence.*`, `javax.annotation.*`.
- **Java 17/21 / Spring Boot 3.x**: Uses `jakarta.validation.constraints.*`, `jakarta.persistence.*`, `jakarta.annotation.*`.
- **Directive**: Inspect target class imports and strictly mirror the active namespace to ensure zero compilation breaks.

## 2. JVM & Testing Framework Compatibility Matrix

| Java Version | Spring Boot Version | Minimum JUnit 5 | Recommended Mockito | Recommended AssertJ |
| --- | --- | --- | --- | --- |
| Java 11 (LTS) | Spring Boot 2.7.x | JUnit 5.8.x | Mockito 4.x / 5.x | AssertJ 3.22.x |
| Java 17 (LTS) | Spring Boot 3.0 - 3.1 | JUnit 5.9.x | Mockito 5.3+ | AssertJ 3.24.x |
| Java 21 (LTS) | Spring Boot 3.2.x+ | JUnit 5.10.x+ | Mockito 5.7+ (ByteBuddy 1.14+) | AssertJ 3.25.x+ |

## 3. Mockito 5 & Java 21 Specifics

- **Mockito 5**: Defaults to the inline mock maker using ByteBuddy. Mocks final classes/methods without extra configuration.
- **Avoid Legacy Hacks**: Avoid deprecated Powermock. Rely on standard Mockito 5 static and constructor mocking.
- **Constructor Injection**: Instantiate classes directly via constructor injection:
```java
new OrderService(mockRepo, mockClient)
```
