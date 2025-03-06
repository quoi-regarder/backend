package fr.quoi_regarder.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.quoi_regarder.commons.enums.ErrorStatus;
import fr.quoi_regarder.dto.response.ApiResponse;
import fr.quoi_regarder.security.service.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {
    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    @Value("${management.endpoints.web.base-path:/actuator}")
    private String actuatorBasePath;

    @Value("${rate.limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // Skip rate limiting if disabled
        if (!rateLimitEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestURI = request.getRequestURI();

        // Skip rate limiting for actuator endpoints
        String fullActuatorPath = contextPath + actuatorBasePath;
        if (requestURI.startsWith(fullActuatorPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Skip rate limiting for Swagger/OpenAPI documentation
        if (requestURI.contains("/swagger-ui") || requestURI.contains("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check if request is allowed by rate limiter
        if (!rateLimitService.isAllowed()) {
            // Create error response using ApiResponse
            ApiResponse<?> errorResponse = ApiResponse.error(
                    "Too many requests. Please try again later.",
                    ErrorStatus.RATE_LIMIT_EXCEEDED,
                    "API rate limit has been exceeded",
                    HttpStatus.TOO_MANY_REQUESTS
            );

            // Configure HTTP response
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            // Write error response as JSON
            objectMapper.writeValue(response.getOutputStream(), errorResponse);
            return;
        }

        // Continue with the request
        filterChain.doFilter(request, response);
    }
}