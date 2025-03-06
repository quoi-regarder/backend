package fr.quoi_regarder.security.config;

import fr.quoi_regarder.security.filter.ActuatorFilter;
import fr.quoi_regarder.security.filter.JwtAuthenticationFilter;
import fr.quoi_regarder.security.filter.RateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ActuatorFilter actuatorFilter;
    private final RateLimitFilter rateLimitFilter;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers(
                                        "/auth/**",                     // All routes related to authentication (login, register, etc.)
                                        "/social/**",                   // All routes related to social authentication
                                        "/reset-password/**",           // All routes related to password reset
                                        "/verify-email/**",             // All routes related to email verification
                                        "/avatars/**",                  // Access to avatars

                                        "/swagger-ui.html",              // Swagger UI documentation
                                        "/swagger-ui/**",                // Swagger UI documentation
                                        "/v3/api-docs",                  // API documentation
                                        "/v3/api-docs/**"                // API documentation
                                ).permitAll()
                                .requestMatchers(
                                        "/actuator",                       // Actuator base path
                                        "/actuator/**"                     // Actuator endpoints with full path
                                ).hasAuthority("ACTUATOR")                 // Requires ACTUATOR authority
                                .requestMatchers(
                                        "/stats/**"                        // Statistics routes
                                ).hasAuthority("STATS")                   // Requires STATS authority
                                .requestMatchers(
                                        "/api/auth/logout"                 // Logout route
                                ).authenticated()                          // Explicitly requires authentication for logout
                                .anyRequest().authenticated()              // All other requests require authentication
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(actuatorFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(frontendUrl));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization"));

        configuration.setMaxAge(3600L);


        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}