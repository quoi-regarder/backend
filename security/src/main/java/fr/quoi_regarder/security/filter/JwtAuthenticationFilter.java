package fr.quoi_regarder.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.quoi_regarder.commons.enums.ErrorStatus;
import fr.quoi_regarder.dto.response.ApiResponse;
import fr.quoi_regarder.exception.exceptions.JwtExpiresException;
import fr.quoi_regarder.security.service.JwtService;
import fr.quoi_regarder.security.service.TokenServiceSimple;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenServiceSimple tokenService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        try {
            final String userEmail = jwtService.extractUsername(jwt);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (userEmail != null && authentication == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                boolean isTokenActive = tokenService.isTokenValid(userEmail, jwt);

                if (jwtService.isTokenValid(jwt, userDetails) && isTokenActive) {
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }

            filterChain.doFilter(request, response);
        } catch (JwtExpiresException ex) {
            // Create error response using ApiResponse
            ApiResponse<?> errorResponse = ApiResponse.error(
                    "JWT token has expired",
                    ErrorStatus.JWT_TOKEN_EXPIRED,
                    ex.getMessage(),
                    HttpStatus.UNAUTHORIZED
            );

            // Configure HTTP response
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            // Write error response as JSON
            objectMapper.writeValue(response.getOutputStream(), errorResponse);
        }
    }
}