package cz.cvut.fel.bp.userservice.configuration;

import cz.cvut.fel.bp.userservice.security.CustomJwtConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
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
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomJwtConverter customJWTConverter;

    @Value("${app.security.jwt.secret}")
    private String jwtSecret;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
                // Todo: configure csrf
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login/**",
                                "/api/auth/register/**",
                                "/api/auth/test/**",
                                "/api/internal/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenResolver(request -> {
                            String uri = request.getRequestURI();
                            if (uri.startsWith("/api/auth/login") || uri.startsWith("/api/auth/register")) {
                                return null;
                            }
                            DefaultBearerTokenResolver resolver = new DefaultBearerTokenResolver();
                            return resolver.resolve(request);
                        })
                        .jwt(jwt -> jwt
                                .decoder(customJwtDecoder())
                                .jwtAuthenticationConverter(customJWTConverter)
                        )
                );

        return http.build();
    }

    /**
     */
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
