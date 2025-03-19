package fr.quoi_regarder.security.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;


/**
 * Configuration for Server-Sent Events handling.
 */
@Configuration
public class SseConfiguration implements WebMvcConfigurer {

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        // Configure text/event-stream content type
        configurer.mediaType("stream", MediaType.TEXT_EVENT_STREAM);
    }

    @Bean
    public FilterRegistrationBean<SseHeadersFilter> sseHeadersFilter() {
        FilterRegistrationBean<SseHeadersFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new SseHeadersFilter());
        registrationBean.addUrlPatterns("/api/notifications/*");
        return registrationBean;
    }

    /**
     * Filter to set appropriate headers for SSE responses.
     */
    public static class SseHeadersFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {

            response.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("X-Accel-Buffering", "no"); // Important for Nginx

            filterChain.doFilter(request, response);
        }
    }
}