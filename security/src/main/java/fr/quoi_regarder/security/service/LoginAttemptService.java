package fr.quoi_regarder.security.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {
    private static final int MAX_ATTEMPTS = 5;
    // Delay before resetting the attempts counter (30 minutes)
    private static final int RESET_DELAY_MILLIS = 30 * 60 * 1000;

    // Cache to store login attempts by client identifier
    private final Cache<String, Integer> attemptsCache = Caffeine.newBuilder()
            .expireAfterWrite(RESET_DELAY_MILLIS, TimeUnit.MILLISECONDS)
            .maximumSize(10_000)
            .build();

    // Cache to store the timestamp of the last failed attempt
    private final Cache<String, Long> blockTimeCache = Caffeine.newBuilder()
            .expireAfterWrite(RESET_DELAY_MILLIS, TimeUnit.MILLISECONDS)
            .maximumSize(10_000)
            .build();

    /**
     * Increments the failed login attempts counter for the current client
     */
    public void loginFailed() {
        String clientId = getClientIdentifier();
        Integer attempts = attemptsCache.getIfPresent(clientId);
        int newAttempts = (attempts == null) ? 1 : attempts + 1;
        attemptsCache.put(clientId, newAttempts);

        // Store the block time if this attempt exceeds the threshold
        if (newAttempts >= MAX_ATTEMPTS) {
            blockTimeCache.put(clientId, System.currentTimeMillis());
        }
    }

    /**
     * Resets the attempts counter for the current client
     */
    public void loginSucceeded() {
        String clientId = getClientIdentifier();
        attemptsCache.invalidate(clientId);
        blockTimeCache.invalidate(clientId);
    }

    /**
     * Checks if the current client is blocked
     *
     * @return true if the client is blocked
     */
    public boolean isBlocked() {
        String clientId = getClientIdentifier();
        Integer attempts = attemptsCache.getIfPresent(clientId);
        return attempts != null && attempts >= MAX_ATTEMPTS;
    }

    /**
     * Gets the number of remaining attempts before blocking
     *
     * @return the number of remaining attempts
     */
    public int getRemainingAttempts() {
        String clientId = getClientIdentifier();
        Integer attempts = attemptsCache.getIfPresent(clientId);
        return Math.max(MAX_ATTEMPTS - (attempts == null ? 0 : attempts), 0);
    }

    /**
     * Gets the remaining time in minutes before the block is reset for the current client
     *
     * @return the remaining time in minutes, or 0 if not blocked
     */
    public int getResetDelay() {
        String clientId = getClientIdentifier();
        Integer attempts = attemptsCache.getIfPresent(clientId);
        if (attempts == null || attempts < MAX_ATTEMPTS) {
            return 0;
        }

        Long blockTime = blockTimeCache.getIfPresent(clientId);
        if (blockTime == null) {
            return 0;
        }

        long elapsedMillis = System.currentTimeMillis() - blockTime;
        long remainingMillis = Math.max(0, RESET_DELAY_MILLIS - elapsedMillis);

        // Convert to minutes and round up
        return (int) Math.ceil(remainingMillis / (60 * 1000.0));
    }

    /**
     * Gets a unique identifier for the current client based on IP and User-Agent
     *
     * @return the client identifier
     */
    private String getClientIdentifier() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }

        HttpServletRequest request = attributes.getRequest();
        String ip = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");

        // If no User-Agent, fallback to IP only
        if (userAgent == null || userAgent.isEmpty()) {
            return ip;
        }

        // Combine IP and User-Agent hash for better identification
        return ip + ":" + userAgent.hashCode();
    }

    /**
     * Gets the client's IP address
     *
     * @return the IP address
     */
    private String getClientIP(HttpServletRequest request) {
        final String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}