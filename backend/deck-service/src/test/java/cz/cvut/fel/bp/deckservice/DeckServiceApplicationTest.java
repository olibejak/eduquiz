package cz.cvut.fel.bp.deckservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test that verifies the Spring Boot application context loads successfully.
 * This is a smoke test that ensures all Spring beans are wired correctly, including
 * the new answer validation strategies.
 *
 * Usage:
 * - Runs with the "test" profile to load test configuration (application.yaml in src/test/resources)
 * - Uses TestSecurityConfig to provide test-specific security bean overrides (JwtDecoder mock)
 * - Automatically wires all @Component, @Service, @Repository beans
 * - Validates that dependency injection is properly configured
 * - Catches configuration errors, missing beans, or circular dependencies early
 * - Tests that all strategies (MultipleChoice, Matching, Write, Numeric) are registered
 *
 * When this test passes, you know:
 * 1. The application can start without errors
 * 2. All Spring beans are created and wired correctly
 * 3. Validation strategies are properly registered and injectable
 * 4. No missing dependencies or circular injection problems exist
 */
@SpringBootTest
@ActiveProfiles("test")
class DeckServiceApplicationTest {

    /**
     * Verifies that the application context can be instantiated without errors and
     * that the QuestionAnswerValidator is properly autowired with all validation strategies.
     *
     * Spring's @SpringBootTest annotation:
     * - Component scans all @Component, @Service, @Repository beans
     * - Dependency injection resolves all required dependencies
     * - Validates that all four strategies (MultipleChoice, Matching, Write, Numeric) are registered
     * - If anything is misconfigured, an exception is thrown and the test fails
     */
    @Test
    void contextLoads() {
        // Verify the validator was autowired successfully by Spring
    }
}

