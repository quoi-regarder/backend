package fr.quoi_regarder.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.quoi_regarder.commons.enums.ErrorStatus;
import fr.quoi_regarder.dto.response.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class ActuatorFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Value("${actuator.api.token}")
    private String actuatorApiToken;

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    @Value("${management.endpoints.web.base-path:/actuator}")
    private String actuatorBasePath;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String fullActuatorPath = contextPath + actuatorBasePath;
        String requestURI = request.getRequestURI();

        if (requestURI.startsWith(fullActuatorPath)) {
            String authHeader = request.getHeader("X-Actuator-Token");

            if (authHeader == null || !authHeader.equals(actuatorApiToken)) {
                // Create error response for missing or invalid token
                ApiResponse<?> errorResponse = ApiResponse.error(
                        "Unauthorized access to actuator endpoints",
                        ErrorStatus.UNAUTHORIZED,
                        HttpStatus.UNAUTHORIZED
                );

                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                objectMapper.writeValue(response.getOutputStream(), errorResponse);
                return;
            }

            // Valid token, set authentication
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    "actuator",
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ACTUATOR"))
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}