package fr.quoi_regarder.security.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class RateLimitService {
    // Rate limits configuration
    private static final int ANONYMOUS_LIMIT = 50; // 50 requests per minute for anonymous users
    private static final int AUTHENTICATED_LIMIT = 200; // 200 requests per minute for authenticated users

    private final HttpServletRequest request;

    // Cache of buckets, keyed by IP or username
    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    /**
     * Checks if the current request is allowed based on rate limit
     *
     * @return true if allowed, false if rate limit exceeded
     */
    public boolean isAllowed() {
        String key = getKey();
        Bucket bucket = resolveBucket(key);
        return bucket.tryConsume(1);
    }

    /**
     * Gets the key for the current request (username if authenticated, IP otherwise)
     *
     * @return the key for the rate limit bucket
     */
    private String getKey() {
        // Try to get user identity
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal())) {
            return "user:" + authentication.getName();
        }

        // Fall back to IP address
        return "ip:" + getClientIP();
    }

    /**
     * Gets the client's IP address
     *
     * @return the IP address
     */
    private String getClientIP() {
        final String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Creates or retrieves the bucket for the given key
     *
     * @param key the bucket key
     * @return the rate limit bucket
     */
    private Bucket resolveBucket(String key) {
        return bucketCache.computeIfAbsent(key, this::createNewBucket);
    }

    /**
     * Creates a new bucket with appropriate limits based on the key
     *
     * @param key the bucket key
     * @return a new rate limit bucket
     */
    private Bucket createNewBucket(String key) {
        if (key.startsWith("user:")) {
            return createAuthenticatedBucket();
        } else {
            return createAnonymousBucket();
        }
    }

    /**
     * Creates a bucket for anonymous users
     *
     * @return the rate limit bucket
     */
    private Bucket createAnonymousBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(ANONYMOUS_LIMIT)
                .refillIntervally(ANONYMOUS_LIMIT, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Creates a bucket for authenticated users
     *
     * @return the rate limit bucket
     */
    private Bucket createAuthenticatedBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(AUTHENTICATED_LIMIT)
                .refillIntervally(AUTHENTICATED_LIMIT, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}