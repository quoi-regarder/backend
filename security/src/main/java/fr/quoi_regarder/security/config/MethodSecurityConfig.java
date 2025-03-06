package fr.quoi_regarder.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Configuration to enable method level security.
 * This class allows the use of security annotations like @PreAuthorize.
 */
@Configuration
@EnableMethodSecurity
public class MethodSecurityConfig {

    /**
     * Defines the security expression handler for methods.
     * Required to use custom SpEL expressions in @PreAuthorize.
     */
    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        return new DefaultMethodSecurityExpressionHandler();
    }
}