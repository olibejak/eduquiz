package cz.cvut.fel.bp.deckservice.configuration;

import cz.cvut.fel.bp.deckservice.security.CustomJwtConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;

/**
 * Security configuration for the application.
 * Configures the application to use JWT tokens for authentication and authorization.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final CustomJwtConverter customJwtConverter;

    @Value("${app.security.jwt.secret}")
    private String jwtSecret;

    /**
     * Configures the security filter chain for the application.
     * @param http the HttpSecurity object to configure
     * @return the configured SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        log.info("Initialize security filter chain for deck-service");
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/decks/search/**",
                                "/api/internal/**"
                                ).permitAll() // Permit every search endpoint
                        .requestMatchers(HttpMethod.GET,
                                "/api/decks/favorites",
                                "/api/decks/my"
                        ).authenticated()
                        .requestMatchers(HttpMethod.GET,
                                "/api/decks",
                                "/api/decks/{id}"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenResolver(request -> {
                            if (request.getRequestURI().startsWith("/api/auth/")) {
                                return null;
                            }
                            DefaultBearerTokenResolver resolver = new DefaultBearerTokenResolver();
                            return resolver.resolve(request);
                        })
                        .jwt(jwt -> jwt
                                .decoder(customJwtDecoder())
                                .jwtAuthenticationConverter(customJwtConverter)
                        )
                );

        return http.build();
    }

    @Bean
    public JwtDecoder customJwtDecoder() {
        SecretKeySpec secretKey = new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role("ADMIN").implies("USER")
                .build();
    }
}
