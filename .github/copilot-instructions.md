# MCP Workshop Java - Copilot Instructions

## Project Overview
This is a **Model Context Protocol (MCP) workshop project** using Spring Boot 4.0.0 and Java 21. It's a minimal starter template designed for building MCP servers or learning MCP integration patterns with Spring Boot.

## Tech Stack
- **Java 21** (language level 21 required)
- **Spring Boot 4.0.0** (Spring Web, Validation, Jackson)
- **Maven** for build management
- **Package structure**: `com.epam.masterclass`

## Personal preferences
- !!! ALWAYS STOP AT SMALLER OPERATIONS FOR ME TO CONTROL THE FLOW !!!
- Don't create MD documentation, unless specifically asked to.

## Build & Run
```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run

# Or run directly from compiled JAR
java -jar target/mcp-workshop-java-1.0-SNAPSHOT.jar
```

## Configuration
- Main config: `src/main/resources/application.yml`
- Data directory: Configurable via `DATA_DIRECTORY` environment variable (default: `./data`)
- Logging: Package level set to INFO, root level WARN

## Code Conventions
- **Main entry point**: `Main.java` with `@SpringBootApplication` annotation
- **Base package**: All new classes should go under `com.epam.masterclass.*` subpackages
- **Package organization** (recommended for expansion):
  - `com.epam.masterclass.controller` - REST endpoints
  - `com.epam.masterclass.service` - Business logic
  - `com.epam.masterclass.model` - DTOs and domain models
  - `com.epam.masterclass.config` - Spring configurations

## Development Guidelines
- Use Java 21 features (pattern matching, records, virtual threads where appropriate)
- Follow Spring Boot 4.x conventions (constructor injection preferred over field injection)
- Jackson is configured for JSON serialization - use `@JsonProperty` for custom field names
- Validation: Use Jakarta Bean Validation annotations (`@Valid`, `@NotNull`, etc.)

## Testing
- Test framework: Spring Boot Test (JUnit 5)
- Test location: `src/test/java` (currently empty - add tests as needed)
- Run tests: `mvn test`

## Key Dependencies
- `spring-boot-starter-web` - REST API support
- `spring-boot-starter-validation` - Bean validation
- `jackson-databind` - JSON processing
- `spring-boot-starter-test` - Testing utilities

## Environment Setup
Ensure `JAVA_HOME` points to JDK 21. Maven wrapper (`.mvn/`) is included for consistent builds.
